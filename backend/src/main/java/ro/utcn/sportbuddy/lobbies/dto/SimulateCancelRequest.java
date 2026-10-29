package ro.utcn.sportbuddy.lobbies.dto;

import jakarta.validation.constraints.NotNull;

// { "userId": 21 }: jucătorul care iese din lobby în simulare
public record SimulateCancelRequest(@NotNull(message = "Alege jucătorul.") Long userId) {}
