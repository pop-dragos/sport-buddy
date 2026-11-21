package ro.utcn.sportbuddy.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import ro.utcn.sportbuddy.auth.model.AuthToken;
import ro.utcn.sportbuddy.auth.model.AuthTokenType;
import ro.utcn.sportbuddy.auth.repository.AuthTokenRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-21T10:00:00Z");

    private AuthTokenRepository repo;
    private AuthTokenService service;

    @BeforeEach
    void setUp() {
        repo = mock(AuthTokenRepository.class);
        service = new AuthTokenService(repo, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private AuthToken created(String raw, AuthTokenType type) {
        var captor = ArgumentCaptor.forClass(AuthToken.class);
        verify(repo).save(captor.capture());
        AuthToken t = captor.getValue();
        when(repo.findByTokenHash(AuthTokenService.hash(raw))).thenReturn(Optional.of(t));
        return t;
    }

    @Test
    void storesOnlyTheHashAndInvalidatesOlderLinks() {
        String raw = service.create(7L, AuthTokenType.RESET_PASSWORD, Duration.ofHours(1));

        AuthToken t = created(raw, AuthTokenType.RESET_PASSWORD);
        assertNotEquals(raw, t.getTokenHash());
        assertEquals(64, t.getTokenHash().length());
        assertEquals(NOW.plus(Duration.ofHours(1)), t.getExpiresAt());
        verify(repo).invalidateAll(7L, AuthTokenType.RESET_PASSWORD, NOW);
    }

    @Test
    void consumeReturnsUserOnceForTheRightType() {
        String raw = service.create(7L, AuthTokenType.VERIFY_EMAIL, Duration.ofHours(24));
        created(raw, AuthTokenType.VERIFY_EMAIL);

        assertTrue(service.consume(raw, AuthTokenType.RESET_PASSWORD).isEmpty());
        assertEquals(Optional.of(7L), service.consume(raw, AuthTokenType.VERIFY_EMAIL));
        verify(repo, times(2)).invalidateAll(7L, AuthTokenType.VERIFY_EMAIL, NOW);
    }

    @Test
    void usedOrExpiredLinksDoNotWork() {
        String raw = service.create(7L, AuthTokenType.RESET_PASSWORD, Duration.ofHours(1));
        AuthToken t = created(raw, AuthTokenType.RESET_PASSWORD);

        t.markUsed(NOW);
        assertTrue(service.consume(raw, AuthTokenType.RESET_PASSWORD).isEmpty());

        AuthToken expired = new AuthToken(7L, AuthTokenType.RESET_PASSWORD, "h", NOW.minusSeconds(1));
        when(repo.findByTokenHash(AuthTokenService.hash("vechi"))).thenReturn(Optional.of(expired));
        assertTrue(service.consume("vechi", AuthTokenType.RESET_PASSWORD).isEmpty());
        assertTrue(service.consume(null, AuthTokenType.RESET_PASSWORD).isEmpty());
    }

    @Test
    void sentRecentlyLooksAtTheLastLink() {
        AuthToken t = new AuthToken(7L, AuthTokenType.VERIFY_EMAIL, "h", NOW.plusSeconds(3600));
        ReflectionTestUtils.setField(t, "createdAt", NOW.minusSeconds(30));
        when(repo.findTopByUserIdAndTypeOrderByCreatedAtDesc(7L, AuthTokenType.VERIFY_EMAIL)).thenReturn(Optional.of(t));

        assertTrue(service.sentRecently(7L, AuthTokenType.VERIFY_EMAIL, Duration.ofSeconds(60)));
        assertFalse(service.sentRecently(7L, AuthTokenType.VERIFY_EMAIL, Duration.ofSeconds(20)));
        assertFalse(service.sentRecently(8L, AuthTokenType.VERIFY_EMAIL, Duration.ofSeconds(60)));
        verify(repo, never()).save(any());
    }
}
