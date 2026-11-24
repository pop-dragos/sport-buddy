package ro.utcn.sportbuddy.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.auth.dto.ChangeEmailRequest;
import ro.utcn.sportbuddy.auth.dto.TokenRequest;
import ro.utcn.sportbuddy.auth.model.AuthTokenType;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.common.i18n.RequestLanguage;
import ro.utcn.sportbuddy.common.mail.EmailTexts;
import ro.utcn.sportbuddy.common.mail.MailService;
import ro.utcn.sportbuddy.users.dto.AuthAccount;
import ro.utcn.sportbuddy.users.dto.UserMe;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Duration;
import java.util.Map;

// Schimbarea emailului din Setări: adresa nouă primește un link de confirmare, iar cea veche un mesaj de
// avertizare. Până la confirmare, contul rămâne pe adresa veche (se poate conecta cu ea, primește emailurile).
@Service
public class EmailChangeService {

    private static final Duration LINK_VALIDITY = Duration.ofHours(24);

    private final UserService userService;
    private final AuthTokenService tokens;
    private final MailService mail;
    private final PasswordEncoder passwordEncoder;
    private final String frontendUrl;

    public EmailChangeService(UserService userService, AuthTokenService tokens, MailService mail,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.userService = userService;
        this.tokens = tokens;
        this.mail = mail;
        this.passwordEncoder = passwordEncoder;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    @Transactional
    public UserMe request(Long userId, ChangeEmailRequest req) {
        AuthAccount account = account(userId);
        String email = req.newEmail().trim();
        if (email.equalsIgnoreCase(account.email())) {
            throw invalid("newEmail", "Acesta este deja emailul contului.");
        }
        // cine are parolă o confirmă: altfel, un telefon lăsat deblocat ar fi de ajuns ca să-ți ia cineva contul
        if (account.passwordHash() != null && (req.currentPassword() == null
                || !passwordEncoder.matches(req.currentPassword(), account.passwordHash()))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WRONG_PASSWORD", "Parola curentă este greșită.",
                    Map.of("currentPassword", "Parola curentă este greșită."));
        }
        if (userService.emailExists(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Există deja un cont cu acest email.");
        }

        boolean sameAsPending = email.equalsIgnoreCase(userService.getMe(userId).pendingEmail());
        // aceeași adresă cerută de mai multe ori la rând: nu trimitem încă un email în același minut
        if (sameAsPending && tokens.sentRecently(userId, AuthTokenType.CHANGE_EMAIL, AuthService.RESEND_WINDOW)) {
            return userService.getMe(userId);
        }
        userService.setPendingEmail(userId, email);
        sendConfirmation(userId, email, account.fullName());
        if (!sameAsPending) {
            EmailTexts.Text warning = EmailTexts.emailChangeWarning(RequestLanguage.current(),
                    account.fullName(), email);
            mail.sendNotice(account.email(), warning.subject(), warning.greeting(), warning.body(), warning.footer());
        }
        return userService.getMe(userId);
    }

    public void resend(Long userId) {
        AuthAccount account = account(userId);
        String pending = userService.getMe(userId).pendingEmail();
        if (pending == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Nu există nicio schimbare de email în așteptare.");
        }
        if (!tokens.sentRecently(userId, AuthTokenType.CHANGE_EMAIL, AuthService.RESEND_WINDOW)) {
            sendConfirmation(userId, pending, account.fullName());
        }
    }

    // Linkurile deja trimise nu mai merg.
    @Transactional
    public UserMe cancel(Long userId) {
        account(userId);
        userService.setPendingEmail(userId, null);
        tokens.invalidate(userId, AuthTokenType.CHANGE_EMAIL);
        return userService.getMe(userId);
    }

    // Din linkul primit la adresa nouă. Merge și fără să fii conectat (de exemplu, din telefon). Întoarce noul email.
    @Transactional
    public String confirm(TokenRequest req) {
        Long id = tokens.consume(req.token(), AuthTokenType.CHANGE_EMAIL)
                .filter(userId -> userService.findAccount(userId).isPresent())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_LINK",
                        "Linkul de confirmare nu mai este valabil. Cere unul nou din Setări."));
        return userService.applyPendingEmail(id);
    }

    private void sendConfirmation(Long userId, String email, String fullName) {
        String token = tokens.create(userId, AuthTokenType.CHANGE_EMAIL, LINK_VALIDITY);
        EmailTexts.Text text = EmailTexts.confirmNewEmail(RequestLanguage.current(), fullName);
        mail.sendLink(email, text.subject(), text.greeting(), text.body(), text.button(),
                frontendUrl + "/confirm-email?token=" + token, text.footer());
    }

    private AuthAccount account(Long userId) {
        return userService.findAccount(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Contul nu există."));
    }

    private static ApiException invalid(String field, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.", Map.of(field, message));
    }
}
