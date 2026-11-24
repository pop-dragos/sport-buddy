package ro.utcn.sportbuddy.common.events;

// Organizatorul a acceptat sau a refuzat cererea de alăturare a lui userId.
public record JoinRequestDecidedEvent(Long lobbyId, Long userId, boolean accepted) {}
