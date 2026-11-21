package ro.utcn.sportbuddy.users.dto;

// Ce are nevoie modulul de autentificare despre un cont activ. Nu iese niciodată din backend.
public record AuthAccount(Long id, String fullName, String email, String passwordHash,
                          boolean emailVerified, String googleSubject, int tokenVersion) {}
