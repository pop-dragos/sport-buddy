package ro.utcn.sportbuddy.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.auth.model.AuthToken;
import ro.utcn.sportbuddy.auth.model.AuthTokenType;
import ro.utcn.sportbuddy.auth.repository.AuthTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class AuthTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthTokenRepository repository;
    private final Clock clock;

    @Autowired
    public AuthTokenService(AuthTokenRepository repository) {
        this(repository, Clock.systemUTC());
    }

    AuthTokenService(AuthTokenRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    // Întoarce valoarea care merge în link. În baza de date ajunge doar hash-ul ei.
    @Transactional
    public String create(Long userId, AuthTokenType type, Duration validFor) {
        Instant now = clock.instant();
        repository.invalidateAll(userId, type, now);
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(new AuthToken(userId, type, hash(raw), now.plus(validFor)));
        return raw;
    }

    // Id-ul utilizatorului dacă linkul e bun, de tipul cerut, nefolosit și neexpirat. După asta nu mai merge.
    @Transactional
    public Optional<Long> consume(String raw, AuthTokenType type) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        Instant now = clock.instant();
        return repository.findByTokenHash(hash(raw.trim()))
                .filter(t -> t.getType() == type && t.isUsable(now))
                .map(t -> {
                    repository.invalidateAll(t.getUserId(), type, now);
                    return t.getUserId();
                });
    }

    // Limită ca să nu se poată trimite zeci de emailuri către cineva.
    public boolean sentRecently(Long userId, AuthTokenType type, Duration window) {
        Instant limit = clock.instant().minus(window);
        return repository.findTopByUserIdAndTypeOrderByCreatedAtDesc(userId, type)
                .map(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(limit))
                .orElse(false);
    }

    static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
