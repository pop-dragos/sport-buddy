package ro.utcn.sportbuddy.ratings.dto;

// Element din „topPlayers" (GET /dashboard/home, docs/api.md)
public record TopPlayerDto(Long userId, String username, String avatarUrl, Double average, int reviewCount) {
}
