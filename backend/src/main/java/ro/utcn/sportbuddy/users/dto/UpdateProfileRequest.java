package ro.utcn.sportbuddy.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

// Toate câmpurile sunt opționale: se trimit doar cele schimbate. Telefonul și descrierea se șterg trimițându-le goale.
public record UpdateProfileRequest(
        String fullName,
        @Email(message = "Email invalid.") String email,
        String phone,
        @Min(value = 16, message = "Vârsta minimă este 16.")
        @Max(value = 99, message = "Vârsta maximă este 99.") Integer age,
        Long cityId,
        @Size(max = 300, message = "Descrierea poate avea maximum 300 de caractere.") String description) {}
