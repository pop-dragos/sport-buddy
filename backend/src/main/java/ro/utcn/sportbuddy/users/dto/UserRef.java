package ro.utcn.sportbuddy.users.dto;

// Ce pot afla alte module despre un utilizator ca să-l afișeze (autor de postare, participant): fără email sau telefon.
public record UserRef(Long id, String username, String fullName, String avatarUrl) {}
