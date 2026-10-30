package ro.utcn.sportbuddy.dashboard.dto;

import ro.utcn.sportbuddy.lobbies.dto.LobbySummary;
import ro.utcn.sportbuddy.lobbies.dto.TodoCounts;
import ro.utcn.sportbuddy.ratings.dto.TopPlayerDto;
import ro.utcn.sportbuddy.users.dto.ProfileCompletion;

import java.util.List;

// GET /dashboard/home (docs/api.md). Vremea și feed-ul le cere frontend-ul separat.
public record DashboardDto(
        LobbySummary nextMatch,                 // null dacă nu am niciun meci programat
        TodoCounts todo,
        List<LobbySummary> recommended,
        List<TopPlayerDto> topPlayers,
        ProfileCompletion profileCompletion) {
}
