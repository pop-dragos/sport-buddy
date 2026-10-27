package ro.utcn.sportbuddy.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Introdu username-ul sau emailul.") String identifier,
        @NotBlank(message = "Introdu parola.") String password,
        Boolean rememberMe) {}