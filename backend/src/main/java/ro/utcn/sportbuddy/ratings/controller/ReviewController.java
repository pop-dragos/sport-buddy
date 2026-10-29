package ro.utcn.sportbuddy.ratings.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.ratings.dto.PendingReviewDto;
import ro.utcn.sportbuddy.ratings.dto.SubmitReviewsRequest;
import ro.utcn.sportbuddy.ratings.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/lobbies/{lobbyId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/pending")
    public List<PendingReviewDto> pending(@PathVariable Long lobbyId) {
        return reviewService.getPending(lobbyId, CurrentUser.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void submit(@PathVariable Long lobbyId, @Valid @RequestBody SubmitReviewsRequest req) {
        reviewService.submit(lobbyId, CurrentUser.id(), req.reviews());
    }
}
