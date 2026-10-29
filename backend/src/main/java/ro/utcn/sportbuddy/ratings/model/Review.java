package ro.utcn.sportbuddy.ratings.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reviews",
       uniqueConstraints = @UniqueConstraint(columnNames = {"lobby_id", "reviewer_id", "target_user_id"}))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // null doar pentru review-urile de test din RatingSeeder (nu există încă lobby-uri de test)
    private Long lobbyId;

    // null pentru penalizările generate de sistem
    private Long reviewerId;

    @Column(nullable = false)
    private Long targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType type;

    @Column(nullable = false)
    private Integer stars;

    private String comment;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Review() {}

    public Review(Long lobbyId, Long reviewerId, Long targetUserId, ReviewType type, int stars) {
        this.lobbyId = lobbyId;
        this.reviewerId = reviewerId;
        this.targetUserId = targetUserId;
        this.type = type;
        this.stars = stars;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getLobbyId() { return lobbyId; }
    public Long getReviewerId() { return reviewerId; }
    public Long getTargetUserId() { return targetUserId; }
    public ReviewType getType() { return type; }
    public Integer getStars() { return stars; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
