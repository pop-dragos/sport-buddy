package ro.utcn.sportbuddy.users.dto;

import jakarta.validation.constraints.NotBlank;

// Regulile parolei noi (PasswordPolicy) se verifică în UserService.
public record ChangePasswordRequest(
        @NotBlank(message = "Introdu parola curentă.") String currentPassword,
        @NotBlank(message = "Introdu parola nouă.") String newPassword) {}
