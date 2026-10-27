package ro.utcn.sportbuddy.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import ro.utcn.sportbuddy.users.dto.SportLevelDto;
import ro.utcn.sportbuddy.users.model.AuthProvider;

import java.util.List;

public record RegisterRequest(
        @NotBlank(message = "Numele este obligatoriu.") String fullName,
        @NotBlank(message = "Emailul este obligatoriu.") @Email(message = "Email invalid.") String email,
        String password,
        @NotNull(message = "Vârsta este obligatorie.")
        @Min(value = 16, message = "Vârsta minimă este 16.")
        @Max(value = 99, message = "Vârsta maximă este 99.") Integer age,
        @NotEmpty(message = "Alege cel puțin un sport.") List<@Valid SportLevelDto> sports,
        String phone,
        Long cityId,
        @Size(max = 300, message = "Descrierea poate avea maximum 300 de caractere.") String description,
        AuthProvider authProvider) {}