package ro.utcn.sportbuddy.ratings.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.utcn.sportbuddy.ratings.dto.RatingStatsDto;
import ro.utcn.sportbuddy.ratings.model.UserRatingStats;
import ro.utcn.sportbuddy.ratings.repository.UserRatingStatsRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Repository-ul e simulat cu Mockito, deci testul nu are nevoie de baza de date.
class RatingServiceTest {

    private UserRatingStatsRepository statsRepository;
    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        statsRepository = mock(UserRatingStatsRepository.class);
        ratingService = new RatingService(statsRepository, new RatingCalculator());
    }

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
        UserRatingStats stats = new UserRatingStats(21L);
        stats.setReviewCount(12);
        stats.setAvgRating(4.2333);
        stats.setAttendedCount(8);
        stats.setLateCancelCount(2);
        stats.setNoShowCount(1);
        when(statsRepository.findById(21L)).thenReturn(Optional.of(stats));

        RatingStatsDto dto = ratingService.getStats(21L);

        assertEquals(4.2, dto.average());
        assertEquals(12, dto.reviewCount());
        assertEquals(80, dto.reliabilityPercent());
    }

    @Test
    void reliabilityIsEmptyWhenUserHasReviewsButNoMatches() {
        UserRatingStats stats = new UserRatingStats(33L);
        stats.setReviewCount(1);
        stats.setAvgRating(2.0);
        when(statsRepository.findById(33L)).thenReturn(Optional.of(stats));

        RatingStatsDto dto = ratingService.getStats(33L);

        assertEquals(2.0, dto.average());
        assertNull(dto.reliabilityPercent());
    }
}
