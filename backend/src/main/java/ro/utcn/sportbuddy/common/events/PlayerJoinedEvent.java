package ro.utcn.sportbuddy.common.events;

// Organizatorul a acceptat o cerere de alăturare (propunerea din docs/api.md, secțiunea 4).
public record PlayerJoinedEvent(Long lobbyId, Long userId) {}
