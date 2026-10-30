package ro.utcn.sportbuddy.dashboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.utcn.sportbuddy.dashboard.dto.DashboardDto;
import ro.utcn.sportbuddy.lobbies.dto.LobbySummary;
import ro.utcn.sportbuddy.lobbies.dto.TodoCounts;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.ratings.dto.RatingStatsDto;
import ro.utcn.sportbuddy.ratings.dto.TopPlayerDto;
import ro.utcn.sportbuddy.ratings.service.RatingService;
import ro.utcn.sportbuddy.users.dto.ProfileCompletion;
import ro.utcn.sportbuddy.users.dto.UserMe;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Toate cele trei service-uri sunt simulate; se verifică doar cum se compune răspunsul.
class DashboardServiceTest {

    private static final Long ANA = 1L;
    private static final Long CITY = 4L;

    private LobbyService lobbyService;
    private RatingService ratingService;
    private UserService userService;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        lobbyService = mock(LobbyService.class);
        ratingService = mock(RatingService.class);
        userService = mock(UserService.class);
        dashboardService = new DashboardService(lobbyService, ratingService, userService);

        when(lobbyService.myLobbies(ANA, "UPCOMING")).thenReturn(List.of());
        when(lobbyService.openLobbiesInCity(CITY, ANA)).thenReturn(List.of());
        when(ratingService.getStats(ANA)).thenReturn(RatingStatsDto.newUser(ANA));
        when(userService.getMe(ANA)).thenReturn(me(new ProfileCompletion(40, List.of("phone", "avatar"))));
    }

    @Test
    void passesThroughTodoTopPlayersAndProfileCompletion() {
        TodoCounts todo = new TodoCounts(2, 0, 1, 0, 3);
        List<TopPlayerDto> top = List.of(new TopPlayerDto(3L, "ioana", null, 4.8, 9));
        when(lobbyService.todo(ANA)).thenReturn(todo);
        when(ratingService.getTopPlayers(DashboardService.TOP_PLAYERS_LIMIT)).thenReturn(top);

        DashboardDto dto = dashboardService.getHome(ANA, CITY);

        assertEquals(todo, dto.todo());
        assertEquals(top, dto.topPlayers());
        assertEquals(40, dto.profileCompletion().percent());
    }

    @Test
    void nextMatchIsTheFirstUpcoming() {
        when(lobbyService.myLobbies(ANA, "UPCOMING")).thenReturn(List.of(lobby(10L, 4.0), lobby(11L, 3.0)));

        assertEquals(10L, dashboardService.getHome(ANA, CITY).nextMatch().id());
    }

    @Test
    void nextMatchIsEmptyWithoutUpcomingLobbies() {
        assertNull(dashboardService.getHome(ANA, CITY).nextMatch());
    }

    @Test
    void recommendedAreClosestToMyLevelAndLimited() {
        when(ratingService.getStats(ANA)).thenReturn(new RatingStatsDto(ANA, 4.2, 10, 90, List.of()));
        when(lobbyService.openLobbiesInCity(CITY, ANA)).thenReturn(List.of(
                lobby(1L, 2.5), lobby(2L, 4.0), lobby(3L, 4.6), lobby(4L, 3.0), lobby(5L, 4.3)));

        List<LobbySummary> recommended = dashboardService.getHome(ANA, CITY).recommended();

        // distanțe față de 4.2: 5 → 0.1, 2 → 0.2, 3 → 0.4, 4 → 1.2, 1 → 1.7
        assertEquals(List.of(5L, 2L, 3L), recommended.stream().map(LobbySummary::id).toList());
    }

    @Test
    void newUserAndUnratedLobbiesUseDefaultLevel() {
        // ana e nouă (3.5); lobby-ul fără rating contează tot ca 3.5, deci e cel mai potrivit
        when(lobbyService.openLobbiesInCity(CITY, ANA)).thenReturn(List.of(
                lobby(1L, 4.8), lobby(2L, null), lobby(3L, 3.0)));

        List<LobbySummary> recommended = dashboardService.getHome(ANA, CITY).recommended();

        assertEquals(List.of(2L, 3L, 1L), recommended.stream().map(LobbySummary::id).toList());
    }

    @Test
    void equalDistanceKeepsDateOrder() {
        when(lobbyService.openLobbiesInCity(CITY, ANA)).thenReturn(List.of(
                lobby(7L, 3.0), lobby(8L, 4.0), lobby(9L, 3.5)));

        List<LobbySummary> recommended = dashboardService.getHome(ANA, CITY).recommended();

        // 9 e exact la 3.5; 7 și 8 sunt la aceeași distanță, deci rămân în ordinea primită (a datei)
        assertEquals(List.of(9L, 7L, 8L), recommended.stream().map(LobbySummary::id).toList());
    }

    // ---------- ajutătoare ----------

    private static LobbySummary lobby(Long id, Double averageRating) {
        return new LobbySummary(id, 1L, "football", "Fotbal", 10L, "Teren", CITY, "Baia Mare",
                LocalDateTime.of(2026, 11, 5, 18, 0), "OPEN", 10, 6, 4, averageRating, "NONE", false, 0);
    }

    private static UserMe me(ProfileCompletion completion) {
        return new UserMe(ANA, "ana", "Ana Popescu", "ana@demo.ro", null, 21, null, CITY, null,
                LocalDateTime.of(2026, 10, 1, 12, 0), List.of(), completion);
    }
}
