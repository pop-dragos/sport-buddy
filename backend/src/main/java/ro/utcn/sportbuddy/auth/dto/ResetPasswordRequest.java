package ro.utcn.sportbuddy.auth.dto;

import jakarta.validation.constraints.NotBlank;

// Regulile parolei (PasswordPolicy) se verifică în service.
public record ResetPasswordRequest(
        @NotBlank(message = "Linkul nu este complet.") String token,
        @NotBlank(message = "Introdu parola nouă.") String newPassword) {}
