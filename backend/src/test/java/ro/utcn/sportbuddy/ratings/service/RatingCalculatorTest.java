package ro.utcn.sportbuddy.ratings.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Test unitar simplu: nu pornește Spring și nu are nevoie de PostgreSQL.
class RatingCalculatorTest {

    private static final double DELTA = 0.0001;

    private final RatingCalculator calculator = new RatingCalculator();

    @Test
    void firstReviewBecomesTheAverage() {
        assertEquals(4.0, calculator.addReview(null, 0, 4), DELTA);
    }

    @Test
    void reviewIsAddedToExistingAverage() {
        // (4.0 * 3 + 2) / 4 = 3.5
        assertEquals(3.5, calculator.addReview(4.0, 3, 2), DELTA);
    }

    @Test
    void lateCancelPenaltyLowersTheAverage() {
        // (4.5 * 9 + 2) / 10 = 4.25
        assertEquals(4.25, calculator.addReview(4.5, 9, RatingCalculator.LATE_CANCEL_STARS), DELTA);
    }

    @Test
    void noShowForNewUserGivesOneStar() {
        assertEquals(1.0, calculator.addReview(null, 0, RatingCalculator.NO_SHOW_STARS), DELTA);
    }

    @Test
    void starsOutsideRangeAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> calculator.addReview(4.0, 3, 0));
        assertThrows(IllegalArgumentException.class, () -> calculator.addReview(4.0, 3, 6));
    }

    @Test
    void reliabilityIsFullWhenOnlyAttended() {
        assertEquals(100, calculator.reliabilityPercent(5, 0, 0));
    }

    @Test
    void reliabilityMixesLateCancelsAndNoShows() {
        // 8 / (8 + 0.5 * 2 + 1) = 80%
        assertEquals(80, calculator.reliabilityPercent(8, 2, 1));
    }

    @Test
    void reliabilityIsRounded() {
        // 9 / (9 + 0.5) = 94.7% -> 95
        assertEquals(95, calculator.reliabilityPercent(9, 1, 0));
    }

    @Test
    void reliabilityIsZeroWithOnlyNoShows() {
        assertEquals(0, calculator.reliabilityPercent(0, 0, 2));
    }

    @Test
    void reliabilityIsEmptyWithoutHistory() {
        assertNull(calculator.reliabilityPercent(0, 0, 0));
    }
}
