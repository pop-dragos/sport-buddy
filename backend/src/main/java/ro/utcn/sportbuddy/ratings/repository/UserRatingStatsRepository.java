package ro.utcn.sportbuddy.ratings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.ratings.model.UserRatingStats;

public interface UserRatingStatsRepository extends JpaRepository<UserRatingStats, Long> {
}
