package ro.utcn.sportbuddy.common.events;

// Cineva a cerut să intre într-un lobby; hostUserId e organizatorul, cel care decide.
public record JoinRequestedEvent(Long lobbyId, Long requesterId, Long hostUserId) {}
