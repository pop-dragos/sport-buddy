package ro.utcn.sportbuddy.ratings.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Limit;
import ro.utcn.sportbuddy.ratings.dto.RatingStatsDto;
import ro.utcn.sportbuddy.ratings.dto.TopPlayerDto;
import ro.utcn.sportbuddy.ratings.model.Review;
import ro.utcn.sportbuddy.ratings.model.ReviewType;
import ro.utcn.sportbuddy.ratings.model.UserRatingStats;
import ro.utcn.sportbuddy.ratings.repository.ReviewRepository;
import ro.utcn.sportbuddy.ratings.repository.UserRatingStatsRepository;
import ro.utcn.sportbuddy.users.dto.PublicProfile;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

// Repository-urile și UserService sunt simulate cu Mockito, deci testul nu are nevoie de baza de date.
class RatingServiceTest {

    private UserRatingStatsRepository statsRepository;
    private ReviewRepository reviewRepository;
    private UserService userService;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        statsRepository = mock(UserRatingStatsRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        userService = mock(UserService.class);
        ratingService = new RatingService(statsRepository, reviewRepository, new RatingCalculator(), userService);
        when(statsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---------- getStats ----------

    @Test
    void userWithoutStatsIsNew() {
        when(statsRepository.findById(7L)).thenReturn(Optional.empty());

        RatingStatsDto dto = ratingService.getStats(7L);

        assertEquals(7L, dto.userId());
        assertNull(dto.average());
        assertEquals(0, dto.reviewCount());
        assertNull(dto.reliabilityPercent());
        assertTrue(dto.tags().isEmpty());
    }

    @Test
    void existingStatsAreRoundedAndReliabilityIsComputed() {
        when(statsRepository.findById(21L)).thenReturn(Optional.of(stats(21L, 12, 4.2333, 8, 2, 1)));

        RatingStatsDto dto = ratingService.getStats(21L);

        assertEquals(4.2, dto.average());
        assertEquals(12, dto.reviewCount());
        assertEquals(80, dto.reliabilityPercent());
    }

    @Test
    void reliabilityIsEmptyWhenUserHasReviewsButNoMatches() {
        when(statsRepository.findById(33L)).thenReturn(Optional.of(stats(33L, 1, 2.0, 0, 0, 0)));

        RatingStatsDto dto = ratingService.getStats(33L);

        assertEquals(2.0, dto.average());
        assertNull(dto.reliabilityPercent());
    }

    // ---------- recordReview ----------

    @Test
    void firstReviewCreatesStats() {
        when(statsRepository.findById(21L)).thenReturn(Optional.empty());

        ratingService.recordReview(55L, 7L, 21L, 4);

        UserRatingStats saved = savedStats();
        assertEquals(21L, saved.getUserId());
        assertEquals(4.0, saved.getAvgRating());
        assertEquals(1, saved.getReviewCount());

        Review review = savedReview();
        assertEquals(55L, review.getLobbyId());
        assertEquals(7L, review.getReviewerId());
        assertEquals(21L, review.getTargetUserId());
        assertEquals(ReviewType.NORMAL, review.getType());
        assertEquals(4, review.getStars());
    }

    @Test
    void reviewUpdatesExistingAverage() {
        when(statsRepository.findById(21L)).thenReturn(Optional.of(stats(21L, 3, 4.0, 3, 0, 0)));

        ratingService.recordReview(55L, 7L, 21L, 2);

        UserRatingStats saved = savedStats();
        assertEquals(3.5, saved.getAvgRating(), 0.0001); // (4.0 * 3 + 2) / 4
        assertEquals(4, saved.getReviewCount());
    }

    @Test
    void invalidStarsSaveNothing() {
        when(statsRepository.findById(21L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ratingService.recordReview(55L, 7L, 21L, 6));

        verify(reviewRepository, never()).save(any());
        verify(statsRepository, never()).save(any());
    }

    // ---------- penalizări și prezență ----------

    @Test
    void lateCancelAddsTwoStarSystemReview() {
        when(statsRepository.findById(21L)).thenReturn(Optional.of(stats(21L, 9, 4.5, 9, 0, 0)));

        ratingService.applyLateCancel(55L, 21L);

        Review review = savedReview();
        assertEquals(ReviewType.LATE_CANCEL, review.getType());
        assertEquals(2, review.getStars());
        assertNull(review.getReviewerId());

        UserRatingStats stats = savedStats();
        assertEquals(4.25, stats.getAvgRating(), 0.0001); // (4.5 * 9 + 2) / 10
        assertEquals(10, stats.getReviewCount());
        assertEquals(1, stats.getLateCancelCount());
    }

    @Test
    void noShowForNewUserGivesOneStar() {
        when(statsRepository.findById(21L)).thenReturn(Optional.empty());

        ratingService.applyNoShow(55L, 21L);

        assertEquals(ReviewType.NO_SHOW, savedReview().getType());
        UserRatingStats stats = savedStats();
        assertEquals(1.0, stats.getAvgRating());
        assertEquals(1, stats.getNoShowCount());
    }

    @Test
    void attendanceOnlyCountsForReliability() {
        when(statsRepository.findById(21L)).thenReturn(Optional.of(stats(21L, 3, 4.0, 2, 0, 0)));

        ratingService.recordAttendance(21L);

        UserRatingStats stats = savedStats();
        assertEquals(3, stats.getAttendedCount());
        assertEquals(4.0, stats.getAvgRating());
        assertEquals(3, stats.getReviewCount());
        verify(reviewRepository, never()).save(any());
    }

    // ---------- top jucători ----------

    @Test
    void topPlayersSkipInactiveUsersAndRoundAverage() {
        when(statsRepository.findByReviewCountGreaterThanEqualOrderByAvgRatingDescReviewCountDesc(anyInt(), any(Limit.class)))
                .thenReturn(List.of(stats(3L, 9, 4.7777, 9, 0, 0), stats(4L, 6, 4.6, 6, 0, 0)));
        when(userService.isActive(3L)).thenReturn(true);
        when(userService.isActive(4L)).thenReturn(false);
        when(userService.getPublicProfile(3L)).thenReturn(new PublicProfile(3L, "ioana", "Ioana Marin",
                "/api/uploads/avatars/3.png", null, null, LocalDate.now(), List.of()));

        List<TopPlayerDto> top = ratingService.getTopPlayers(5);

        assertEquals(1, top.size());
        TopPlayerDto first = top.get(0);
        assertEquals("ioana", first.username());
        assertEquals("/api/uploads/avatars/3.png", first.avatarUrl());
        assertEquals(4.8, first.average());
        assertEquals(9, first.reviewCount());
        verify(statsRepository).findByReviewCountGreaterThanEqualOrderByAvgRatingDescReviewCountDesc(
                RatingService.TOP_MIN_REVIEWS, Limit.of(5));
    }

    // ---------- ajutătoare ----------

    private static UserRatingStats stats(Long userId, int reviews, Double avg, int attended, int late, int noShow) {
        UserRatingStats s = new UserRatingStats(userId);
        s.setReviewCount(reviews);
        s.setAvgRating(avg);
        s.setAttendedCount(attended);
        s.setLateCancelCount(late);
        s.setNoShowCount(noShow);
        return s;
    }

    private UserRatingStats savedStats() {
        ArgumentCaptor<UserRatingStats> captor = ArgumentCaptor.forClass(UserRatingStats.class);
        verify(statsRepository, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    private Review savedReview() {
        ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(captor.capture());
        return captor.getValue();
    }
}
