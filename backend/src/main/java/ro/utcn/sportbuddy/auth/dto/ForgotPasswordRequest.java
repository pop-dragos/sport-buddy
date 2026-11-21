package ro.utcn.sportbuddy.auth.dto;

import jakarta.validation.constraints.NotBlank;

// folosit și la retrimiterea emailului de confirmare
public record ForgotPasswordRequest(@NotBlank(message = "Introdu username-ul sau emailul.") String identifier) {}
