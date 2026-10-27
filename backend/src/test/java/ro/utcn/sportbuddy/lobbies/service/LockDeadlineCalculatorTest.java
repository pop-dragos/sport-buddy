package ro.utcn.sportbuddy.lobbies.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test unitar simplu: nu pornește Spring și nu are nevoie de PostgreSQL.
class LockDeadlineCalculatorTest {

    // „Acum" fix, ca testele să dea mereu același rezultat.
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 10, 0);

    private final LockDeadlineCalculator calculator = new LockDeadlineCalculator();

    @Test
    void fiveDaysAheadLocksTwelveHoursBefore() {
        LocalDateTime start = NOW.plusDays(5);
        assertEquals(start.minusHours(12), calculator.deadline(NOW, start));
    }

    @Test
    void exactlyThreeDaysAheadLocksTwelveHoursBefore() {
        LocalDateTime start = NOW.plusDays(3);
        assertEquals(start.minusHours(12), calculator.deadline(NOW, start));
    }

    @Test
    void justUnderThreeDaysLocksSixHoursBefore() {
        LocalDateTime start = NOW.plusDays(3).minusMinutes(15);
        assertEquals(start.minusHours(6), calculator.deadline(NOW, start));
    }

    @Test
    void exactlyOneDayAheadLocksSixHoursBefore() {
        LocalDateTime start = NOW.plusDays(1);
        assertEquals(start.minusHours(6), calculator.deadline(NOW, start));
    }

    @Test
    void underOneDayLocksTwoHoursBefore() {
        LocalDateTime start = NOW.plusHours(5);
        assertEquals(start.minusHours(2), calculator.deadline(NOW, start));
    }

    @Test
    void startExactlyThreeHoursAheadIsValid() {
        assertTrue(calculator.isValidStart(NOW, NOW.plusHours(3)));
    }

    @Test
    void startUnderThreeHoursAheadIsRejected() {
        assertFalse(calculator.isValidStart(NOW, NOW.plusHours(2).plusMinutes(45)));
    }

    @Test
    void startInThePastIsRejected() {
        assertFalse(calculator.isValidStart(NOW, NOW.minusDays(1)));
    }

    @Test
    void minutesMustBeMultipleOfFifteen() {
        assertTrue(calculator.isValidStart(NOW, NOW.plusDays(1).withMinute(45)));
        assertFalse(calculator.isValidStart(NOW, NOW.plusDays(1).withMinute(20)));
    }
}
