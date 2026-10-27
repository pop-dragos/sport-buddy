package ro.utcn.sportbuddy.auth.dto;

import ro.utcn.sportbuddy.users.dto.UserMe;

import java.time.LocalDateTime;

public record AuthResponse(String token, LocalDateTime expiresAt, UserMe user) {}
