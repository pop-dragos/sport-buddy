package ro.utcn.sportbuddy.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.auth.dto.ChangeEmailRequest;
import ro.utcn.sportbuddy.auth.service.EmailChangeService;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.users.dto.UserMe;

import java.util.Map;

// Schimbarea emailului contului conectat. Confirmarea din link e în AuthController (/api/auth/confirm-email).
@RestController
@RequestMapping("/api/users/me/email")
public class EmailChangeController {

    private final EmailChangeService emailChangeService;

    public EmailChangeController(EmailChangeService emailChangeService) {
        this.emailChangeService = emailChangeService;
    }

    // Întoarce profilul cu pendingEmail completat.
    @PostMapping
    public UserMe request(@Valid @RequestBody ChangeEmailRequest req) {
        return emailChangeService.request(CurrentUser.id(), req);
    }

    @PostMapping("/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> resend() {
        emailChangeService.resend(CurrentUser.id());
        return Map.of("message", "Am retrimis linkul de confirmare.");
    }

    @DeleteMapping
    public UserMe cancel() {
        return emailChangeService.cancel(CurrentUser.id());
    }
}
