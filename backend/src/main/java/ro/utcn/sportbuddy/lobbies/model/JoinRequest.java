package ro.utcn.sportbuddy.lobbies.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "join_requests")
public class JoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long lobbyId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinRequestStatus status = JoinRequestStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant decidedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Long lobbyId) {
        this.lobbyId = lobbyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public JoinRequestStatus getStatus() {
        return status;
    }

    public void setStatus(JoinRequestStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }
}
