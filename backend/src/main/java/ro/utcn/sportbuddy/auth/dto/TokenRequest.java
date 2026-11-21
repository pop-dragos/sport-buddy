package ro.utcn.sportbuddy.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@NotBlank(message = "Linkul nu este complet.") String token) {}
