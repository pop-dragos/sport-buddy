package ro.utcn.sportbuddy.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// currentPassword: obligatorie doar la conturile care au parolă (cele create cu Google pot să nu aibă).
public record ChangeEmailRequest(
        @NotBlank(message = "Scrie emailul nou.") @Email(message = "Email invalid.") String newEmail,
        String currentPassword) {}
