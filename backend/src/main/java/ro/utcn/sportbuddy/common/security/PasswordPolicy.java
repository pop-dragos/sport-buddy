package ro.utcn.sportbuddy.common.security;

import org.springframework.http.HttpStatus;
import ro.utcn.sportbuddy.common.error.ApiException;

import java.util.Map;
import java.util.Optional;

// Aceleași reguli la crearea contului, la resetare și la schimbarea parolei (frontend-ul le arată la fel).
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    // BCrypt ignoră tot ce trece de 72 de octeți
    public static final int MAX_LENGTH = 72;

    private PasswordPolicy() {}

    public static Optional<String> problem(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return Optional.of("Parola trebuie să aibă minimum " + MIN_LENGTH + " caractere.");
        }
        if (password.length() > MAX_LENGTH) {
            return Optional.of("Parola poate avea maximum " + MAX_LENGTH + " de caractere.");
        }
        if (password.chars().noneMatch(Character::isLetter)) {
            return Optional.of("Parola trebuie să conțină cel puțin o literă.");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            return Optional.of("Parola trebuie să conțină cel puțin o cifră.");
        }
        return Optional.empty();
    }

    // field: numele câmpului din formular, ca eroarea să apară sub el
    public static void check(String password, String field) {
        problem(password).ifPresent(msg -> {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.", Map.of(field, msg));
        });
    }
}
