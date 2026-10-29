package ro.utcn.sportbuddy.ratings.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.ratings.model.Review;
import ro.utcn.sportbuddy.ratings.model.ReviewType;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // a trimis deja notele pentru lobby-ul ăsta?
    boolean existsByLobbyIdAndReviewerIdAndType(Long lobbyId, Long reviewerId, ReviewType type);
}
