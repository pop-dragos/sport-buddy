package ro.utcn.sportbuddy.ratings.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.ratings.model.UserRatingStats;

import java.util.List;

public interface UserRatingStatsRepository extends JpaRepository<UserRatingStats, Long> {

    // „Top jucători": cei cu destule review-uri, după medie; la egalitate, cel cu mai multe review-uri
    List<UserRatingStats> findByReviewCountGreaterThanEqualOrderByAvgRatingDescReviewCountDesc(
            int minReviews, Limit limit);
}
