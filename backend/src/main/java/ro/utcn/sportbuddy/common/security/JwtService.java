package ro.utcn.sportbuddy.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtService {

    // Token-urile de sesiune nu au „purpose”. Cele cu scop (de ex. terminarea înregistrării cu Google)
    // nu pot fi folosite ca sesiune și invers.
    private static final String PURPOSE = "purpose";
    private static final String VERSION = "ver";

    public record Session(Long userId, int version, Instant expiresAt) {}

    private final SecretKey key;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(Long userId, int version, Instant expiresAt) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(VERSION, version)
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public String generate(Long userId, Instant expiresAt) {
        return generate(userId, 0, expiresAt);
    }

    public Optional<Session> parseSession(String token) {
        return claims(token)
                .filter(c -> c.get(PURPOSE) == null)
                .map(c -> {
                    Integer ver = c.get(VERSION, Integer.class);
                    // token-urile emise înainte de versiuni nu au „ver” și contează ca versiunea 0
                    return new Session(Long.parseLong(c.getSubject()), ver == null ? 0 : ver,
                            c.getExpiration().toInstant());
                });
    }

    public Optional<Long> parse(String token) {
        return parseSession(token).map(Session::userId);
    }

    public String generatePurpose(String purpose, Map<String, String> data, Instant expiresAt) {
        return Jwts.builder()
                .claim(PURPOSE, purpose)
                .claims(data)
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }

    public Optional<Claims> parsePurpose(String token, String purpose) {
        return claims(token).filter(c -> purpose.equals(c.get(PURPOSE)));
    }

    private Optional<Claims> claims(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
