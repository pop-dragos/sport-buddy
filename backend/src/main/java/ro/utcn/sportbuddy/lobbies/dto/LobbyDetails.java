package ro.utcn.sportbuddy.lobbies.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

// Forma din docs/api.md. Prețurile sunt în lei întregi și goale pentru terenurile gratuite.
public record LobbyDetails(
        Long id,
        SportRef sport,
        FieldRef field,
        CityRef city,
        LocalDateTime dateTime,
        String status,
        HostRef host,
        int maxPlayers,
        int minPlayers,
        int broughtPlayers,
        int currentPlayers,
        int freeSlots,
        Double averageRating,
        int fieldCost,
        LocalDateTime confirmDeadline,
        Integer estimatedPrice,
        Integer priceAtFull,
        Integer finalPrice,
        boolean minNotReached,
        boolean joinClosed,
        String myState,
        @JsonProperty("isHost") boolean isHost,
        List<Participant> participants,
        List<PendingRequest> pendingRequests) {

    public record SportRef(Long id, String code, String name) {}

    public record FieldRef(Long id, String name, String district) {}

    public record CityRef(Long id, String name) {}

    public record HostRef(Long id, String username) {}

    public record Participant(Long userId, String username, @JsonProperty("isHost") boolean isHost,
                              Double rating, int reviewCount, Integer reliabilityPercent,
                              String paymentMethod) {}

    public record PendingRequest(Long requestId, Long userId, String username,
                                 Double rating, Integer reliabilityPercent) {}
}
