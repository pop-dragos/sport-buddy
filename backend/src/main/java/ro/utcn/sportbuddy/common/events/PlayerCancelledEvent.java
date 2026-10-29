package ro.utcn.sportbuddy.common.events;

// Un jucător a ieșit din lobby. late = după confirmare, deci cu penalizare.
public record PlayerCancelledEvent(Long lobbyId, Long userId, boolean late) {}
