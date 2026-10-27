package ro.utcn.sportbuddy.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Introdu parola curentă.") String currentPassword,
        @NotBlank(message = "Introdu parola nouă.")
        @Size(min = 6, message = "Parola trebuie să aibă minimum 6 caractere.") String newPassword) {}
