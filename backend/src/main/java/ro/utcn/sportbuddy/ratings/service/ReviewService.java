package ro.utcn.sportbuddy.ratings.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.lobbies.dto.LobbyDetails;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.ratings.dto.PendingReviewDto;
import ro.utcn.sportbuddy.ratings.dto.SubmitReviewsRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Notele date după meci. Stă separat de RatingService pentru că are nevoie de LobbyService,
// iar LobbyService folosește deja RatingService (altfel ar apărea o dependență circulară).
@Service
public class ReviewService {

    private final LobbyService lobbyService;
    private final RatingService ratingService;

    public ReviewService(LobbyService lobbyService, RatingService ratingService) {
        this.lobbyService = lobbyService;
        this.ratingService = ratingService;
    }

    // Colegii pe care încă nu i-am notat: fără mine, fără absenți, fără jucătorii aduși (nu au cont).
    public List<PendingReviewDto> getPending(Long lobbyId, Long userId) {
        LobbyDetails lobby = reviewableLobby(lobbyId, userId);
        if (ratingService.hasReviewed(lobbyId, userId)) {
            return List.of();
        }
        return colleagues(lobby, userId).stream()
                .map(p -> new PendingReviewDto(p.userId(), p.username()))
                .toList();
    }

    // Toate notele se trimit o dată; întâi se verifică toate, abia apoi se salvează.
    @Transactional
    public void submit(Long lobbyId, Long userId, List<SubmitReviewsRequest.Item> reviews) {
        LobbyDetails lobby = reviewableLobby(lobbyId, userId);
        if (ratingService.hasReviewed(lobbyId, userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "ALREADY_REVIEWED", "Ai trimis deja notele pentru acest meci.");
        }

        Set<Long> allowed = new HashSet<>();
        colleagues(lobby, userId).forEach(p -> allowed.add(p.userId()));
        Set<Long> seen = new HashSet<>();
        for (SubmitReviewsRequest.Item item : reviews) {
            if (!allowed.contains(item.userId()) || !seen.add(item.userId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                        "Poți nota doar colegii care au venit la meci, câte o dată.");
            }
        }

        for (SubmitReviewsRequest.Item item : reviews) {
            ratingService.recordReview(lobbyId, userId, item.userId(), item.score());
        }
    }

    // Lobby-ul există (altfel 404 din getDetails), eu am fost acolo și meciul s-a încheiat.
    private LobbyDetails reviewableLobby(Long lobbyId, Long userId) {
        LobbyDetails lobby = lobbyService.getDetails(lobbyId, userId);
        boolean iCame = lobby.participants().stream()
                .anyMatch(p -> p.userId().equals(userId) && !Boolean.FALSE.equals(p.attended()));
        if (!iCame) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_PARTICIPANT", "Doar cei care au jucat dau note.");
        }
        if (!"FINISHED".equals(lobby.status())) {
            throw new ApiException(HttpStatus.CONFLICT, "LOBBY_NOT_FINISHED", "Notele se dau după meci.");
        }
        return lobby;
    }

    // attended e null cât organizatorul nu a salvat prezența; până atunci îi considerăm veniți.
    private List<LobbyDetails.Participant> colleagues(LobbyDetails lobby, Long userId) {
        return lobby.participants().stream()
                .filter(p -> !p.userId().equals(userId))
                .filter(p -> !Boolean.FALSE.equals(p.attended()))
                .toList();
    }
}
