package ro.utcn.sportbuddy.auth.model;

import jakarta.persistence.*;

import java.time.Instant;

// Linkurile trimise pe email (confirmare cont, resetare parolă). Se păstrează doar hash-ul:
// cine vede baza de date nu poate folosi linkurile. Un link merge o singură dată.
@Entity
@Table(name = "auth_tokens", indexes = @Index(name = "idx_auth_tokens_user_type", columnList = "userId, type"))
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthTokenType type;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected AuthToken() {}

    public AuthToken(Long userId, AuthTokenType type, String tokenHash, Instant expiresAt) {
        this.userId = userId;
        this.type = type;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public boolean isUsable(Instant now) {
        return usedAt == null && now.isBefore(expiresAt);
    }

    public void markUsed(Instant now) {
        usedAt = now;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public AuthTokenType getType() { return type; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
