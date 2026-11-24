package ro.utcn.sportbuddy.notifications.dto;

import java.time.LocalDateTime;

// type: JOIN_REQUESTED, JOIN_ACCEPTED, JOIN_REJECTED, LOBBY_LOCKED, LOBBY_CANCELLED, FRIEND_REQUEST, FRIEND_ACCEPTED.
// lobbyId, actorId, actorName și subject (sportul) pot lipsi, după tip.
public record NotificationDto(Long id, String type, Long lobbyId, Long actorId, String actorName, String subject,
                              LocalDateTime createdAt, boolean read) {}
