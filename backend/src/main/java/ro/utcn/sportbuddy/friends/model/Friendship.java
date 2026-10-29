package ro.utcn.sportbuddy.friends.model;

import jakarta.persistence.*;

import java.time.Instant;

// Un singur rând pe pereche de utilizatori: cererea, apoi prietenia. Cine a trimis cererea rămâne requester.
@Entity
@Table(name = "friendships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "addressee_id"}))
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requesterId;

    @Column(nullable = false)
    private Long addresseeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant respondedAt;

    protected Friendship() {}

    public Friendship(Long requesterId, Long addresseeId) {
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public void accept() {
        status = FriendshipStatus.ACCEPTED;
        respondedAt = Instant.now();
    }

    public void decline() {
        status = FriendshipStatus.DECLINED;
        respondedAt = Instant.now();
    }

    public Long otherThan(Long userId) {
        return requesterId.equals(userId) ? addresseeId : requesterId;
    }

    public Long getId() { return id; }
    public Long getRequesterId() { return requesterId; }
    public Long getAddresseeId() { return addresseeId; }
    public FriendshipStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getRespondedAt() { return respondedAt; }

    public boolean isPending() { return status == FriendshipStatus.PENDING; }
    public boolean isAccepted() { return status == FriendshipStatus.ACCEPTED; }
}
