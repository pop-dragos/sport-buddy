package ro.utcn.sportbuddy.ratings.dto;

import java.util.List;

// average și reliabilityPercent sunt null pentru utilizatorii noi (interfața afișează „nou").
public record RatingStatsDto(Long userId, Double average, int reviewCount,
                             Integer reliabilityPercent, List<String> tags) {

    public static RatingStatsDto newUser(Long userId) {
        return new RatingStatsDto(userId, null, 0, null, List.of());
    }
}
