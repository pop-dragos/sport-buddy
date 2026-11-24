package ro.utcn.sportbuddy.auth.service;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.auth.dto.*;
import ro.utcn.sportbuddy.auth.model.AuthTokenType;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.common.i18n.RequestLanguage;
import ro.utcn.sportbuddy.common.mail.EmailTexts;
import ro.utcn.sportbuddy.common.mail.MailService;
import ro.utcn.sportbuddy.common.security.JwtService;
import ro.utcn.sportbuddy.common.security.PasswordPolicy;
import ro.utcn.sportbuddy.users.dto.AuthAccount;
import ro.utcn.sportbuddy.users.model.AuthProvider;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Service
public class AuthService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");
    private static final Duration SHORT_SESSION = Duration.ofDays(1);
    private static final Duration LONG_SESSION = Duration.ofDays(30);
    private static final Duration VERIFY_LINK_VALIDITY = Duration.ofHours(24);
    private static final Duration RESET_LINK_VALIDITY = Duration.ofHours(1);
    // cel mult un email de același fel pe minut pentru un cont
    static final Duration RESEND_WINDOW = Duration.ofSeconds(60);
    private static final Duration GOOGLE_SIGNUP_VALIDITY = Duration.ofMinutes(30);
    private static final String GOOGLE_SIGNUP = "google-signup";
    private static final Duration SETUP_VALIDITY = Duration.ofHours(1);
    private static final String SIGNUP_SETUP = "signup-setup";

    // Profilele returnate de conectarea simulată (iCloud mereu; Google doar cât timp nu e configurat).
    private static final Map<AuthProvider, SocialResponse.Profile> SOCIAL_PROFILES = Map.of(
            AuthProvider.GOOGLE, new SocialResponse.Profile("Utilizator Google", "utilizator@gmail.com"),
            AuthProvider.ICLOUD, new SocialResponse.Profile("Utilizator iCloud", "utilizator@icloud.com"));

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService tokens;
    private final MailService mail;
    private final GoogleTokenVerifier google;
    private final String frontendUrl;

    public AuthService(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder,
                       AuthTokenService tokens, MailService mail, GoogleTokenVerifier google,
                       @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.mail = mail;
        this.google = google;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    public AuthConfigResponse config() {
        return new AuthConfigResponse(google.clientId(), mail.isConfigured());
    }

    // ---------- cont nou ----------

    // Cont cu parolă: se creează neconfirmat și primește linkul de confirmare pe email.
    // Cont Google (cu signupToken): emailul e confirmat de Google, deci intră direct.
    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        if (req.googleSignupToken() != null && !req.googleSignupToken().isBlank()) {
            return registerWithGoogle(req);
        }
        AuthProvider provider = req.authProvider() == null ? AuthProvider.LOCAL : req.authProvider();
        if (provider == AuthProvider.GOOGLE && google.isConfigured()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Pentru un cont Google folosește butonul „Continuă cu Google”.");
        }
        String hash = null;
        if (provider == AuthProvider.LOCAL) {
            PasswordPolicy.check(req.password(), "password");
            hash = passwordEncoder.encode(req.password());
        }
        String email = req.email().trim();
        if (userService.emailExists(email)) throw emailTaken();

        boolean local = provider == AuthProvider.LOCAL;
        String fullName = req.fullName().trim();
        Long id = userService.createUser(fullName, email, hash, req.age(), req.phone(),
                req.cityId(), req.description(), provider, req.sports(), !local, null);
        if (!local) return RegisterResponse.loggedIn(issueToken(id, false));

        sendVerification(id, email, fullName);
        String setupToken = jwtService.generatePurpose(SIGNUP_SETUP, Map.of("uid", String.valueOf(id)),
                Instant.now().plus(SETUP_VALIDITY));
        return RegisterResponse.verificationSent(email, setupToken);
    }

    private RegisterResponse registerWithGoogle(RegisterRequest req) {
        Claims claims = jwtService.parsePurpose(req.googleSignupToken(), GOOGLE_SIGNUP)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "SIGNUP_EXPIRED",
                        "Înregistrarea cu Google a expirat. Apasă din nou „Continuă cu Google”."));
        String subject = claims.get("sub_google", String.class);
        String email = claims.get("email", String.class);

        // formular trimis de două ori: contul există deja, doar conectăm
        var existing = userService.findAccountByGoogleSubject(subject);
        if (existing.isPresent()) return RegisterResponse.loggedIn(issueToken(existing.get().id(), false));
        if (userService.emailExists(email)) throw emailTaken();

        Long id = userService.createUser(req.fullName().trim(), email, null, req.age(), req.phone(),
                req.cityId(), req.description(), AuthProvider.GOOGLE, req.sports(), true, subject);
        return RegisterResponse.loggedIn(issueToken(id, false));
    }

    // Poza aleasă la crearea contului, încărcată înainte de confirmarea emailului (cu setupToken).
    public void uploadSignupAvatar(String setupToken, byte[] content) {
        Long id = jwtService.parsePurpose(setupToken, SIGNUP_SETUP)
                .map(c -> Long.valueOf(c.get("uid", String.class)))
                .filter(userId -> userService.findAccount(userId).isPresent())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                        "Poza se poate adăuga din Setări, după conectare."));
        userService.updateAvatar(id, content);
    }

    // ---------- conectare ----------

    public AuthResponse login(LoginRequest req) {
        AuthAccount account = userService.findAccount(req.identifier().trim())
                .filter(a -> a.passwordHash() != null && passwordEncoder.matches(req.password(), a.passwordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "Utilizator sau parolă incorecte."));
        if (!account.emailVerified()) {
            // parola e corectă, deci putem spune ce lipsește; retrimitem linkul dacă nu s-a trimis de curând
            if (!tokens.sentRecently(account.id(), AuthTokenType.VERIFY_EMAIL, RESEND_WINDOW)) {
                sendVerification(account.id(), account.email(), account.fullName());
            }
            throw new ApiException(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED",
                    "Contul nu este confirmat încă. Ți-am trimis pe email linkul de confirmare.");
        }
        return issueToken(account.id(), Boolean.TRUE.equals(req.rememberMe()));
    }

    // Prima dată: contul Google nou trebuie completat (sporturi, vârstă), sau se leagă de contul
    // existent cu același email. Apoi: conectare directă.
    @Transactional
    public SocialResponse google(GoogleLoginRequest req) {
        if (!google.isConfigured()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "GOOGLE_NOT_CONFIGURED",
                    "Conectarea cu Google nu este configurată pe server.");
        }
        var profile = google.verify(req.credential())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "GOOGLE_INVALID",
                        "Conectarea cu Google nu a reușit. Încearcă din nou."));
        if (profile.email() == null || !profile.emailVerified()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "GOOGLE_EMAIL_UNVERIFIED",
                    "Contul Google nu are un email confirmat.");
        }
        boolean remember = Boolean.TRUE.equals(req.rememberMe());

        var byGoogle = userService.findAccountByGoogleSubject(profile.subject());
        if (byGoogle.isPresent()) return SocialResponse.loggedIn(issueToken(byGoogle.get().id(), remember));

        var byEmail = userService.findAccountByEmail(profile.email());
        if (byEmail.isPresent()) {
            userService.linkGoogle(byEmail.get().id(), profile.subject());
            return SocialResponse.loggedIn(issueToken(byEmail.get().id(), remember));
        }

        String name = profile.name() == null || profile.name().isBlank()
                ? profile.email().substring(0, profile.email().indexOf('@')) : profile.name();
        String signupToken = jwtService.generatePurpose(GOOGLE_SIGNUP,
                Map.of("sub_google", profile.subject(), "email", profile.email()),
                Instant.now().plus(GOOGLE_SIGNUP_VALIDITY));
        return SocialResponse.signupNeeded(new SocialResponse.Profile(name, profile.email()), signupToken);
    }

    // Conectare simulată (iCloud; Google doar cât timp nu e configurat Client ID-ul).
    public SocialResponse social(SocialRequest req) {
        if (req.provider() == AuthProvider.LOCAL) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Furnizor invalid.");
        }
        if (req.provider() == AuthProvider.GOOGLE && google.isConfigured()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Folosește butonul „Continuă cu Google”.");
        }
        SocialResponse.Profile profile = SOCIAL_PROFILES.get(req.provider());
        return userService.findIdByEmail(profile.email())
                .map(id -> SocialResponse.loggedIn(issueToken(id, false)))
                .orElseGet(() -> SocialResponse.signupNeeded(profile));
    }

    // ---------- confirmare email ----------

    // Linkul din email conectează direct utilizatorul.
    @Transactional
    public AuthResponse verifyEmail(TokenRequest req) {
        Long id = tokens.consume(req.token(), AuthTokenType.VERIFY_EMAIL)
                .filter(userId -> userService.findAccount(userId).isPresent())
                .orElseThrow(() -> invalidLink("Linkul de confirmare nu mai este valabil. "
                        + "Conectează-te ca să primești altul."));
        userService.markEmailVerified(id);
        return issueToken(id, false);
    }

    // Nu spune dacă există contul (aceeași reacție oricum).
    public void resendVerification(ForgotPasswordRequest req) {
        userService.findAccount(req.identifier().trim())
                .filter(a -> !a.emailVerified())
                .filter(a -> !tokens.sentRecently(a.id(), AuthTokenType.VERIFY_EMAIL, RESEND_WINDOW))
                .ifPresent(a -> sendVerification(a.id(), a.email(), a.fullName()));
    }

    // ---------- parolă uitată ----------

    // Nu spune dacă există contul. Merge și pentru conturile Google fără parolă: își pot seta una.
    public void forgotPassword(ForgotPasswordRequest req) {
        userService.findAccount(req.identifier().trim())
                .filter(a -> !tokens.sentRecently(a.id(), AuthTokenType.RESET_PASSWORD, RESEND_WINDOW))
                .ifPresent(a -> {
                    String token = tokens.create(a.id(), AuthTokenType.RESET_PASSWORD, RESET_LINK_VALIDITY);
                    // emailul pleacă în limba în care e deschisă aplicația
                    EmailTexts.Text text = EmailTexts.resetPassword(RequestLanguage.current(), a.fullName());
                    mail.sendLink(a.email(), text.subject(), text.greeting(), text.body(), text.button(),
                            frontendUrl + "/reset-password?token=" + token, text.footer());
                });
    }

    // Parola se verifică înaintea linkului, ca o parolă respinsă să nu consume linkul.
    // După resetare, toate sesiunile vechi se închid.
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        PasswordPolicy.check(req.newPassword(), "newPassword");
        Long id = tokens.consume(req.token(), AuthTokenType.RESET_PASSWORD)
                .filter(userId -> userService.findAccount(userId).isPresent())
                .orElseThrow(() -> invalidLink("Linkul de resetare nu mai este valabil. Cere unul nou."));
        userService.resetPassword(id, req.newPassword());
    }

    // ---------- comune ----------

    private void sendVerification(Long userId, String email, String fullName) {
        String token = tokens.create(userId, AuthTokenType.VERIFY_EMAIL, VERIFY_LINK_VALIDITY);
        EmailTexts.Text text = EmailTexts.verifyEmail(RequestLanguage.current(), fullName);
        mail.sendLink(email, text.subject(), text.greeting(), text.body(), text.button(),
                frontendUrl + "/verify-email?token=" + token, text.footer());
    }

    private AuthResponse issueToken(Long userId, boolean rememberMe) {
        int version = userService.findAccount(userId).map(AuthAccount::tokenVersion).orElse(0);
        Instant expiresAt = Instant.now().plus(rememberMe ? LONG_SESSION : SHORT_SESSION);
        String token = jwtService.generate(userId, version, expiresAt);
        return new AuthResponse(token, LocalDateTime.ofInstant(expiresAt, ZONE), userService.getMe(userId));
    }

    private static ApiException emailTaken() {
        return new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Există deja un cont cu acest email.");
    }

    private static ApiException invalidLink(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_LINK", message);
    }
}
