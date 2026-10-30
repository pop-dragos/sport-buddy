package ro.utcn.sportbuddy.dashboard.service;

import org.springframework.stereotype.Service;
import ro.utcn.sportbuddy.dashboard.dto.DashboardDto;
import ro.utcn.sportbuddy.lobbies.dto.LobbySummary;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.ratings.service.RatingService;
import ro.utcn.sportbuddy.users.service.UserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Prima pagină dintr-o singură cerere. Nu are tabele: doar adună date de la celelalte module, prin service-urile lor.
@Service
public class DashboardService {

    static final int RECOMMENDED_LIMIT = 3;
    static final int TOP_PLAYERS_LIMIT = 3;
    // nivelul pentru cine nu are încă rating (utilizator sau lobby), ca în prototip
    static final double DEFAULT_LEVEL = 3.5;

    private final LobbyService lobbyService;
    private final RatingService ratingService;
    private final UserService userService;

    public DashboardService(LobbyService lobbyService, RatingService ratingService, UserService userService) {
        this.lobbyService = lobbyService;
        this.ratingService = ratingService;
        this.userService = userService;
    }

    public DashboardDto getHome(Long userId, Long cityId) {
        List<LobbySummary> upcoming = lobbyService.myLobbies(userId, "UPCOMING");
        return new DashboardDto(
                upcoming.isEmpty() ? null : upcoming.get(0),
                lobbyService.todo(userId),
                recommended(userId, cityId),
                ratingService.getTopPlayers(TOP_PLAYERS_LIMIT),
                userService.getMe(userId).profileCompletion());
    }

    // „Recomandate pentru nivelul tău": lobby-urile la care pot intra, cele cu nivelul cel mai apropiat de al meu primele.
    // openLobbiesInCity le dă în ordinea datei, iar sortarea e stabilă, deci la egalitate rămâne primul cel mai apropiat în timp.
    private List<LobbySummary> recommended(Long userId, Long cityId) {
        double myLevel = levelOr(ratingService.getStats(userId).average());
        List<LobbySummary> open = new ArrayList<>(lobbyService.openLobbiesInCity(cityId, userId));
        open.sort(Comparator.comparingDouble(l -> Math.abs(levelOr(l.averageRating()) - myLevel)));
        return open.stream().limit(RECOMMENDED_LIMIT).toList();
    }

    private static double levelOr(Double rating) {
        return rating == null ? DEFAULT_LEVEL : rating;
    }
}
