package ro.utcn.sportbuddy.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.auth.dto.*;
import org.springframework.web.multipart.MultipartFile;
import ro.utcn.sportbuddy.auth.service.AuthService;
import ro.utcn.sportbuddy.auth.service.EmailChangeService;

import ro.utcn.sportbuddy.common.i18n.Messages;
import ro.utcn.sportbuddy.common.i18n.RequestLanguage;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailChangeService emailChangeService;

    public AuthController(AuthService authService, EmailChangeService emailChangeService) {
        this.authService = authService;
        this.emailChangeService = emailChangeService;
    }

    @GetMapping("/config")
    public AuthConfigResponse config() {
        return authService.config();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    // poza din formularul de cont nou, cu setupToken-ul primit la /register
    @PostMapping(value = "/signup-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signupAvatar(@RequestHeader("X-Setup-Token") String setupToken,
                             @RequestParam("file") MultipartFile file) throws IOException {
        authService.uploadSignupAvatar(setupToken, file.getBytes());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/google")
    public SocialResponse google(@Valid @RequestBody GoogleLoginRequest req) {
        return authService.google(req);
    }

    // conectare simulată (iCloud)
    @PostMapping("/social")
    public SocialResponse social(@Valid @RequestBody SocialRequest req) {
        return authService.social(req);
    }

    @PostMapping("/verify-email")
    public AuthResponse verifyEmail(@Valid @RequestBody TokenRequest req) {
        return authService.verifyEmail(req);
    }

    // Linkul trimis la emailul nou, după o schimbare cerută din Setări. Merge și fără conectare.
    @PostMapping("/confirm-email")
    public Map<String, String> confirmEmail(@Valid @RequestBody TokenRequest req) {
        String email = emailChangeService.confirm(req);
        return Map.of("message", text("Emailul contului este acum " + email + "."), "email", email);
    }

    // Răspunsul e același dacă există sau nu contul.
    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> resendVerification(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.resendVerification(req);
        return Map.of("message", text("Dacă există un cont neconfirmat, vei primi un email cu linkul de confirmare."));
    }

    // Răspunsul e același dacă există sau nu contul.
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return Map.of("message", text("Dacă există un cont, vei primi un email cu linkul de resetare."));
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return Map.of("message", text("Parola a fost schimbată. Te poți conecta cu parola nouă."));
    }

    // mesajul în limba cererii
    private static String text(String ro) {
        return Messages.translate(ro, RequestLanguage.current());
    }
}
