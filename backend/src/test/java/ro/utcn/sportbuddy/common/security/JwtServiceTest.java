package ro.utcn.sportbuddy.common.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-123";
    private final JwtService jwt = new JwtService(SECRET);

    @Test
    void sessionTokenCarriesUserAndVersion() {
        String token = jwt.generate(7L, 4, Instant.now().plusSeconds(60));
        var s = jwt.parseSession(token).orElseThrow();
        assertEquals(7L, s.userId());
        assertEquals(4, s.version());
    }

    @Test
    void tokensFromBeforeVersioningCountAsVersionZero() {
        String old = Jwts.builder().subject("7").expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        assertEquals(0, jwt.parseSession(old).orElseThrow().version());
    }

    @Test
    void purposeTokensAreNotSessionsAndViceVersa() {
        String purpose = jwt.generatePurpose("google-signup", Map.of("email", "a@b.ro"), Instant.now().plusSeconds(60));
        String session = jwt.generate(7L, Instant.now().plusSeconds(60));

        assertTrue(jwt.parseSession(purpose).isEmpty());
        assertTrue(jwt.parsePurpose(session, "google-signup").isEmpty());
        assertTrue(jwt.parsePurpose(purpose, "alt-scop").isEmpty());
        assertEquals("a@b.ro", jwt.parsePurpose(purpose, "google-signup").orElseThrow().get("email"));
    }

    @Test
    void expiredOrForeignTokensAreRejected() {
        assertTrue(jwt.parseSession(jwt.generate(7L, Instant.now().minusSeconds(5))).isEmpty());
        String foreign = new JwtService("alt-secret-alt-secret-alt-secret-alt-12345")
                .generate(7L, Instant.now().plusSeconds(60));
        assertTrue(jwt.parseSession(foreign).isEmpty());
    }
}
