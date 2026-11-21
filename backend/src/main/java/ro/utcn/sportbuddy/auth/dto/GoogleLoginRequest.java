package ro.utcn.sportbuddy.auth.dto;

import jakarta.validation.constraints.NotBlank;

// credential: token-ul primit de browser de la butonul Google
public record GoogleLoginRequest(@NotBlank(message = "Lipsește răspunsul de la Google.") String credential,
                                 Boolean rememberMe) {}
