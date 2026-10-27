package ro.utcn.sportbuddy.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ro.utcn.sportbuddy.auth.dto.*;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.common.security.JwtService;
import ro.utcn.sportbuddy.users.model.AuthProvider;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Service
public class AuthService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");
    private static final Duration SHORT_SESSION = Duration.ofDays(1);
    private static final Duration LONG_SESSION = Duration.ofDays(30);
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Profilele returnate de conectarea simulată cu Google / iCloud.
    private static final Map<AuthProvider, SocialResponse.Profile> SOCIAL_PROFILES = Map.of(
            AuthProvider.GOOGLE, new SocialResponse.Profile("Utilizator Google", "utilizator@gmail.com"),
            AuthProvider.ICLOUD, new SocialResponse.Profile("Utilizator iCloud", "utilizator@icloud.com"));

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest req) {
        AuthProvider provider = req.authProvider() == null ? AuthProvider.LOCAL : req.authProvider();
        String hash = null;
        if (provider == AuthProvider.LOCAL) {
            if (req.password() == null || req.password().length() < MIN_PASSWORD_LENGTH) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.",
                        Map.of("password", "Parola trebuie să aibă minimum " + MIN_PASSWORD_LENGTH + " caractere."));
            }
            hash = passwordEncoder.encode(req.password());
        }
        String email = req.email().trim();
        if (userService.emailExists(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN", "Există deja un cont cu acest email.");
        }
        Long id = userService.createUser(req.fullName().trim(), email, hash, req.age(), req.phone(),
                req.cityId(), req.description(), provider, req.sports());
        return issueToken(id, false);
    }

    public AuthResponse login(LoginRequest req) {
        var credentials = userService.findCredentials(req.identifier().trim())
                .filter(c -> c.passwordHash() != null
                        && passwordEncoder.matches(req.password(), c.passwordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "Utilizator sau parolă incorecte."));
        return issueToken(credentials.id(), Boolean.TRUE.equals(req.rememberMe()));
    }

    public SocialResponse social(SocialRequest req) {
        if (req.provider() == AuthProvider.LOCAL) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Furnizor invalid.");
        }
        SocialResponse.Profile profile = SOCIAL_PROFILES.get(req.provider());
        return userService.findIdByEmail(profile.email())
                .map(id -> SocialResponse.loggedIn(issueToken(id, false)))
                .orElseGet(() -> SocialResponse.signupNeeded(profile));
    }

    private AuthResponse issueToken(Long userId, boolean rememberMe) {
        Instant expiresAt = Instant.now().plus(rememberMe ? LONG_SESSION : SHORT_SESSION);
        String token = jwtService.generate(userId, expiresAt);
        return new AuthResponse(token, LocalDateTime.ofInstant(expiresAt, ZONE), userService.getMe(userId));
    }
}
