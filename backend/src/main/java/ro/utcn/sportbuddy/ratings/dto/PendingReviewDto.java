package ro.utcn.sportbuddy.ratings.dto;

// Un coleg pe care încă nu l-am notat (GET /lobbies/{id}/reviews/pending)
public record PendingReviewDto(Long userId, String username) {
}
