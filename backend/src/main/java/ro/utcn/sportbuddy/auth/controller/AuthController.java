package ro.utcn.sportbuddy.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.auth.dto.*;
import ro.utcn.sportbuddy.auth.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    // Simulat: nu trimite nimic și nu arată dacă există contul.
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return Map.of("message", "Dacă există un cont, vei primi un email.");
    }

    @PostMapping("/social")
    public SocialResponse social(@Valid @RequestBody SocialRequest req) {
        return authService.social(req);
    }
}
