package ro.utcn.sportbuddy.ratings.service;

import org.springframework.stereotype.Service;
import ro.utcn.sportbuddy.ratings.dto.RatingStatsDto;
import ro.utcn.sportbuddy.ratings.model.UserRatingStats;
import ro.utcn.sportbuddy.ratings.repository.UserRatingStatsRepository;

import java.util.List;

@Service
public class RatingService {

    private final UserRatingStatsRepository statsRepository;
    private final RatingCalculator calculator;

    public RatingService(UserRatingStatsRepository statsRepository, RatingCalculator calculator) {
        this.statsRepository = statsRepository;
        this.calculator = calculator;
    }

    // Folosit și de alte module (lobbies, dashboard). Cine nu are încă statistici e „nou".
    public RatingStatsDto getStats(Long userId) {
        return statsRepository.findById(userId)
                .map(this::toDto)
                .orElseGet(() -> RatingStatsDto.newUser(userId));
    }

    private RatingStatsDto toDto(UserRatingStats stats) {
        Double average = stats.getAvgRating() == null
                ? null
                : Math.round(stats.getAvgRating() * 10) / 10.0;
        Integer reliability = calculator.reliabilityPercent(
                stats.getAttendedCount(), stats.getLateCancelCount(), stats.getNoShowCount());
        // Etichetele nu au încă tabelă în entitati.md; până se decide, lista e goală.
        return new RatingStatsDto(stats.getUserId(), average, stats.getReviewCount(), reliability, List.of());
    }
}
