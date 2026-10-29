package ro.utcn.sportbuddy.common.events;

// Lobby-ul a fost anulat. late = organizatorul a anulat după confirmare, deci cu penalizare.
public record LobbyCancelledEvent(Long lobbyId, Long hostUserId, boolean late) {}
