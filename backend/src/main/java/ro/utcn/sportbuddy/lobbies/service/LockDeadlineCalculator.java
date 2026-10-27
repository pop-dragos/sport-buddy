package ro.utcn.sportbuddy.lobbies.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

// Termenul de confirmare al unui lobby. Nu știe de baza de date, ca să poată fi testat direct.
@Component
public class LockDeadlineCalculator {

    // Meciul trebuie creat cu cel puțin atât timp înainte.
    public static final Duration MIN_TIME_BEFORE_START = Duration.ofHours(3);

    // 3 zile sau mai mult: cu 12 ore înainte; între 1 și 3 zile: cu 6 ore; sub o zi: cu 2 ore.
    public LocalDateTime deadline(LocalDateTime now, LocalDateTime startAt) {
        Duration untilStart = Duration.between(now, startAt);
        if (untilStart.compareTo(Duration.ofDays(3)) >= 0) {
            return startAt.minusHours(12);
        }
        if (untilStart.compareTo(Duration.ofDays(1)) >= 0) {
            return startAt.minusHours(6);
        }
        return startAt.minusHours(2);
    }

    // Ora meciului: cel puțin 3 ore în viitor, minutele multiplu de 15, fără secunde.
    public boolean isValidStart(LocalDateTime now, LocalDateTime startAt) {
        return !startAt.isBefore(now.plus(MIN_TIME_BEFORE_START))
                && startAt.getMinute() % 15 == 0
                && startAt.getSecond() == 0
                && startAt.getNano() == 0;
    }
}
