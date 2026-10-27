package ro.utcn.sportbuddy.ratings.model;

import jakarta.persistence.*;
import java.time.Instant;

// Tabel de cache: se actualizează la fiecare review nou, ca citirea ratingului să fie rapidă.
@Entity
@Table(name = "user_rating_stats")
public class UserRatingStats {

    @Id
    private Long userId;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    private Double avgRating;

    @Column(nullable = false)
    private Integer attendedCount = 0;

    @Column(nullable = false)
    private Integer lateCancelCount = 0;

    @Column(nullable = false)
    private Integer noShowCount = 0;

    @Column(nullable = false)
    private Instant updatedAt;

    protected UserRatingStats() {}

    public UserRatingStats(Long userId) {
        this.userId = userId;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getUserId() { return userId; }
    public Integer getReviewCount() { return reviewCount; }
    public Double getAvgRating() { return avgRating; }
    public Integer getAttendedCount() { return attendedCount; }
    public Integer getLateCancelCount() { return lateCancelCount; }
    public Integer getNoShowCount() { return noShowCount; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }
    public void setAttendedCount(Integer attendedCount) { this.attendedCount = attendedCount; }
    public void setLateCancelCount(Integer lateCancelCount) { this.lateCancelCount = lateCancelCount; }
    public void setNoShowCount(Integer noShowCount) { this.noShowCount = noShowCount; }
}
