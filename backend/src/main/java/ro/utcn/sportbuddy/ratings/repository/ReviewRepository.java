package ro.utcn.sportbuddy.ratings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.ratings.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
