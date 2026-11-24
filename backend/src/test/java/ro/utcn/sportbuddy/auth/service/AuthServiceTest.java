package ro.utcn.sportbuddy.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.utcn.sportbuddy.auth.dto.*;
import ro.utcn.sportbuddy.auth.model.AuthTokenType;
import ro.utcn.sportbuddy.auth.service.GoogleTokenVerifier.GoogleProfile;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.common.mail.MailService;
import ro.utcn.sportbuddy.common.security.JwtService;
import ro.utcn.sportbuddy.users.dto.AuthAccount;
import ro.utcn.sportbuddy.users.dto.SportLevelDto;
import ro.utcn.sportbuddy.users.dto.UserMe;
import ro.utcn.sportbuddy.users.model.AuthProvider;
import ro.utcn.sportbuddy.users.model.SportLevel;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-123";

    private UserService userService;
    private JwtService jwtService;
    private PasswordEncoder encoder;
    private AuthTokenService tokens;
    private MailService mail;
    private GoogleTokenVerifier google;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        jwtService = new JwtService(SECRET);
        encoder = new BCryptPasswordEncoder();
        tokens = mock(AuthTokenService.class);
        mail = mock(MailService.class);
        google = mock(GoogleTokenVerifier.class);
        authService = new AuthService(userService, jwtService, encoder, tokens, mail, google,
                "http://localhost:5173/");
        when(userService.getMe(any())).thenAnswer(i -> userMe(i.getArgument(0)));
        when(tokens.create(any(), any(), any())).thenReturn("link-secret");
    }

    private static UserMe userMe(Long id) {
        return new UserMe(id, "ana", "Ana Popescu", "ana@exemplu.ro", null, 21, null, null, null,
                LocalDateTime.now(), List.of(), null);
    }

    private static RegisterRequest register(String password, AuthProvider provider) {
        return new RegisterRequest("Ana Popescu", "ana@exemplu.ro", password, 21,
                List.of(new SportLevelDto(1L, SportLevel.BEGINNER)), null, null, null, provider);
    }

    private static AuthAccount account(Long id, String hash, boolean verified, int version) {
        return new AuthAccount(id, "Ana Popescu", "ana@exemplu.ro", hash, verified, null, version);
    }

    private void accountExists(AuthAccount a) {
        when(userService.findAccount("ana")).thenReturn(Optional.of(a));
        when(userService.findAccount(a.id())).thenReturn(Optional.of(a));
    }

    private void createUserReturns(Long id) {
        when(userService.createUser(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(id);
    }

    // ---------- cont nou ----------

    @Test
    void registerRejectsWeakPasswords() {
        for (String weak : List.of("1234567", "parolafaracifre", "12345678")) {
            ApiException e = assertThrows(ApiException.class,
                    () -> authService.register(register(weak, AuthProvider.LOCAL)), weak);
            assertEquals("VALIDATION_ERROR", e.getCode());
            assertTrue(e.getFields().containsKey("password"));
        }
        verify(userService, never()).createUser(any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userService.emailExists("ana@exemplu.ro")).thenReturn(true);
        ApiException e = assertThrows(ApiException.class,
                () -> authService.register(register("parola123", AuthProvider.LOCAL)));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("EMAIL_TAKEN", e.getCode());
    }

    @Test
    void registerCreatesUnverifiedAccountAndSendsConfirmationLink() {
        createUserReturns(7L);

        RegisterResponse res = authService.register(register("parola123", AuthProvider.LOCAL));

        var hash = ArgumentCaptor.forClass(String.class);
        verify(userService).createUser(eq("Ana Popescu"), eq("ana@exemplu.ro"), hash.capture(), eq(21),
                any(), any(), any(), eq(AuthProvider.LOCAL), any(), eq(false), isNull());
        assertTrue(encoder.matches("parola123", hash.getValue()));
        assertNull(res.token());
        assertTrue(res.needsVerification());
        verify(tokens).create(eq(7L), eq(AuthTokenType.VERIFY_EMAIL), any());
        verify(mail).sendLink(eq("ana@exemplu.ro"), any(), any(), any(), any(),
                eq("http://localhost:5173/verify-email?token=link-secret"), any());
    }

    @Test
    void setupTokenFromRegisterAllowsOnlyTheSignupPhoto() {
        createUserReturns(7L);
        when(userService.findAccount(7L)).thenReturn(Optional.of(account(7L, "h", false, 0)));
        RegisterResponse res = authService.register(register("parola123", AuthProvider.LOCAL));

        // nu e o sesiune
        assertTrue(jwtService.parse(res.setupToken()).isEmpty());
        byte[] photo = {1, 2, 3};
        authService.uploadSignupAvatar(res.setupToken(), photo);
        verify(userService).updateAvatar(7L, photo);

        String session = jwtService.generate(7L, java.time.Instant.now().plusSeconds(60));
        assertThrows(ApiException.class, () -> authService.uploadSignupAvatar(session, photo));
    }

    @Test
    void confirmationEmailIsSentInTheLanguageOfTheRequest() {
        createUserReturns(7L);
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        request.addHeader("Accept-Language", "en");
        org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(
                new org.springframework.web.context.request.ServletRequestAttributes(request));
        try {
            authService.register(register("parola123", AuthProvider.LOCAL));
        } finally {
            org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
        }

        verify(mail).sendLink(eq("ana@exemplu.ro"), eq("Confirm your SportBuddy account"), eq("Welcome, Ana Popescu!"),
                any(), eq("Confirm email"), any(), any());
    }

    @Test
    void simulatedSocialRegisterLogsInDirectly() {
        createUserReturns(8L);

        RegisterResponse res = authService.register(register(null, AuthProvider.ICLOUD));

        verify(userService).createUser(any(), any(), isNull(), any(), any(), any(), any(),
                eq(AuthProvider.ICLOUD), any(), eq(true), isNull());
        assertEquals(Optional.of(8L), jwtService.parse(res.token()));
        verifyNoInteractions(mail);
    }

    // ---------- conectare ----------

    @Test
    void loginWithCorrectPasswordSucceeds() {
        accountExists(account(7L, encoder.encode("demo123"), true, 3));

        AuthResponse res = authService.login(new LoginRequest("ana", "demo123", false));

        var session = jwtService.parseSession(res.token()).orElseThrow();
        assertEquals(7L, session.userId());
        assertEquals(3, session.version());
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() {
        accountExists(account(7L, encoder.encode("demo123"), true, 0));
        ApiException e = assertThrows(ApiException.class,
                () -> authService.login(new LoginRequest("ana", "gresita", false)));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
        assertEquals("INVALID_CREDENTIALS", e.getCode());
    }

    @Test
    void loginWithUnknownUserIsUnauthorized() {
        when(userService.findAccount("nimeni")).thenReturn(Optional.empty());
        ApiException e = assertThrows(ApiException.class,
                () -> authService.login(new LoginRequest("nimeni", "x", null)));
        assertEquals("INVALID_CREDENTIALS", e.getCode());
    }

    @Test
    void socialAccountWithoutPasswordCannotLogInWithPassword() {
        accountExists(account(7L, null, true, 0));
        assertThrows(ApiException.class, () -> authService.login(new LoginRequest("ana", "orice", false)));
    }

    @Test
    void unverifiedAccountCannotLogInAndGetsANewLink() {
        accountExists(account(7L, encoder.encode("demo123"), false, 0));

        ApiException e = assertThrows(ApiException.class,
                () -> authService.login(new LoginRequest("ana", "demo123", false)));

        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals("EMAIL_NOT_VERIFIED", e.getCode());
        verify(tokens).create(eq(7L), eq(AuthTokenType.VERIFY_EMAIL), any());
    }

    @Test
    void unverifiedLoginDoesNotResendTooOften() {
        accountExists(account(7L, encoder.encode("demo123"), false, 0));
        when(tokens.sentRecently(eq(7L), eq(AuthTokenType.VERIFY_EMAIL), any())).thenReturn(true);

        assertThrows(ApiException.class, () -> authService.login(new LoginRequest("ana", "demo123", false)));

        verify(tokens, never()).create(any(), any(), any());
    }

    @Test
    void rememberMeExtendsSessionToThirtyDays() {
        accountExists(account(7L, encoder.encode("demo123"), true, 0));

        AuthResponse shortSession = authService.login(new LoginRequest("ana", "demo123", false));
        AuthResponse longSession = authService.login(new LoginRequest("ana", "demo123", true));

        long days = Duration.between(shortSession.expiresAt(), longSession.expiresAt()).toDays();
        assertTrue(days >= 28 && days <= 30, "diferență de zile: " + days);
    }

    // ---------- confirmare email ----------

    @Test
    void verifyEmailConfirmsAccountAndLogsIn() {
        when(tokens.consume("abc", AuthTokenType.VERIFY_EMAIL)).thenReturn(Optional.of(7L));
        when(userService.findAccount(7L)).thenReturn(Optional.of(account(7L, "h", false, 0)));

        AuthResponse res = authService.verifyEmail(new TokenRequest("abc"));

        verify(userService).markEmailVerified(7L);
        assertEquals(Optional.of(7L), jwtService.parse(res.token()));
    }

    @Test
    void verifyEmailWithBadLinkFails() {
        when(tokens.consume("abc", AuthTokenType.VERIFY_EMAIL)).thenReturn(Optional.empty());
        ApiException e = assertThrows(ApiException.class, () -> authService.verifyEmail(new TokenRequest("abc")));
        assertEquals("INVALID_LINK", e.getCode());
        verify(userService, never()).markEmailVerified(any());
    }

    @Test
    void resendVerificationIsSilentForVerifiedOrUnknownAccounts() {
        accountExists(account(7L, "h", true, 0));
        authService.resendVerification(new ForgotPasswordRequest("ana"));
        authService.resendVerification(new ForgotPasswordRequest("nimeni"));
        verifyNoInteractions(mail);
    }

    // ---------- parolă uitată ----------

    @Test
    void forgotPasswordSendsResetLink() {
        accountExists(account(7L, "h", true, 0));

        authService.forgotPassword(new ForgotPasswordRequest(" ana "));

        verify(tokens).create(eq(7L), eq(AuthTokenType.RESET_PASSWORD), eq(Duration.ofHours(1)));
        verify(mail).sendLink(eq("ana@exemplu.ro"), any(), any(), any(), any(),
                contains("/reset-password?token=link-secret"), any());
    }

    @Test
    void forgotPasswordForUnknownAccountSendsNothing() {
        authService.forgotPassword(new ForgotPasswordRequest("nimeni"));
        verifyNoInteractions(mail);
    }

    @Test
    void forgotPasswordIsRateLimited() {
        accountExists(account(7L, "h", true, 0));
        when(tokens.sentRecently(eq(7L), eq(AuthTokenType.RESET_PASSWORD), any())).thenReturn(true);

        authService.forgotPassword(new ForgotPasswordRequest("ana"));

        verifyNoInteractions(mail);
    }

    @Test
    void resetPasswordChecksPolicyBeforeUsingTheLink() {
        ApiException e = assertThrows(ApiException.class,
                () -> authService.resetPassword(new ResetPasswordRequest("abc", "scurt1")));
        assertTrue(e.getFields().containsKey("newPassword"));
        verify(tokens, never()).consume(any(), any());
    }

    @Test
    void resetPasswordWithValidLinkChangesPassword() {
        when(tokens.consume("abc", AuthTokenType.RESET_PASSWORD)).thenReturn(Optional.of(7L));
        when(userService.findAccount(7L)).thenReturn(Optional.of(account(7L, "h", true, 0)));

        authService.resetPassword(new ResetPasswordRequest("abc", "parolaNoua1"));

        verify(userService).resetPassword(7L, "parolaNoua1");
    }

    @Test
    void resetPasswordWithUsedLinkFails() {
        when(tokens.consume("abc", AuthTokenType.RESET_PASSWORD)).thenReturn(Optional.empty());
        ApiException e = assertThrows(ApiException.class,
                () -> authService.resetPassword(new ResetPasswordRequest("abc", "parolaNoua1")));
        assertEquals("INVALID_LINK", e.getCode());
    }

    // ---------- Google ----------

    private void googleReturns(GoogleProfile profile) {
        when(google.isConfigured()).thenReturn(true);
        when(google.verify("cred")).thenReturn(Optional.ofNullable(profile));
    }

    private static final GoogleProfile ANA_GOOGLE =
            new GoogleProfile("sub-1", "ana@gmail.com", true, "Ana Popescu");

    @Test
    void googleLogsInKnownGoogleAccount() {
        googleReturns(ANA_GOOGLE);
        when(userService.findAccountByGoogleSubject("sub-1")).thenReturn(Optional.of(account(7L, null, true, 0)));
        when(userService.findAccount(7L)).thenReturn(Optional.of(account(7L, null, true, 0)));

        SocialResponse res = authService.google(new GoogleLoginRequest("cred", false));

        assertEquals(Optional.of(7L), jwtService.parse(res.token()));
        assertNull(res.needsSignup());
    }

    @Test
    void googleLinksExistingAccountWithSameEmail() {
        googleReturns(ANA_GOOGLE);
        when(userService.findAccountByGoogleSubject("sub-1")).thenReturn(Optional.empty());
        when(userService.findAccountByEmail("ana@gmail.com")).thenReturn(Optional.of(account(7L, "h", true, 0)));

        SocialResponse res = authService.google(new GoogleLoginRequest("cred", false));

        verify(userService).linkGoogle(7L, "sub-1");
        assertNotNull(res.token());
    }

    @Test
    void googleNewUserMustFinishSignupThenRegistersWithoutPassword() {
        googleReturns(ANA_GOOGLE);
        when(userService.findAccountByGoogleSubject("sub-1")).thenReturn(Optional.empty());
        when(userService.findAccountByEmail("ana@gmail.com")).thenReturn(Optional.empty());

        SocialResponse first = authService.google(new GoogleLoginRequest("cred", false));

        assertTrue(first.needsSignup());
        assertEquals("ana@gmail.com", first.profile().email());
        assertNotNull(first.signupToken());
        // token-ul de înregistrare nu poate fi folosit ca sesiune
        assertTrue(jwtService.parse(first.signupToken()).isEmpty());

        createUserReturns(9L);
        // emailul trimis din formular e ignorat: contează cel confirmat de Google
        RegisterResponse res = authService.register(new RegisterRequest("Ana P.", "alt@email.ro", null, 21,
                List.of(new SportLevelDto(1L, SportLevel.BEGINNER)), null, null, null, AuthProvider.GOOGLE,
                first.signupToken()));

        verify(userService).createUser(eq("Ana P."), eq("ana@gmail.com"), isNull(), eq(21), any(), any(), any(),
                eq(AuthProvider.GOOGLE), any(), eq(true), eq("sub-1"));
        assertEquals(Optional.of(9L), jwtService.parse(res.token()));
    }

    @Test
    void googleRegisterWithForgedTokenFails() {
        RegisterRequest req = new RegisterRequest("Ana", "ana@gmail.com", null, 21,
                List.of(new SportLevelDto(1L, SportLevel.BEGINNER)), null, null, null, AuthProvider.GOOGLE,
                jwtService.generate(7L, java.time.Instant.now().plusSeconds(60)));
        ApiException e = assertThrows(ApiException.class, () -> authService.register(req));
        assertEquals("SIGNUP_EXPIRED", e.getCode());
    }

    @Test
    void googleWithInvalidCredentialIsRejected() {
        googleReturns(null);
        ApiException e = assertThrows(ApiException.class,
                () -> authService.google(new GoogleLoginRequest("cred", false)));
        assertEquals("GOOGLE_INVALID", e.getCode());
    }

    @Test
    void simulatedGoogleIsOffOnceRealGoogleIsConfigured() {
        when(google.isConfigured()).thenReturn(true);
        assertThrows(ApiException.class, () -> authService.social(new SocialRequest(AuthProvider.GOOGLE)));
        assertThrows(ApiException.class, () -> authService.register(register(null, AuthProvider.GOOGLE)));
    }
}
