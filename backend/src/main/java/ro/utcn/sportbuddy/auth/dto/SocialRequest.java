package ro.utcn.sportbuddy.auth.dto;

import jakarta.validation.constraints.NotNull;
import ro.utcn.sportbuddy.users.model.AuthProvider;

public record SocialRequest(@NotNull AuthProvider provider) {}