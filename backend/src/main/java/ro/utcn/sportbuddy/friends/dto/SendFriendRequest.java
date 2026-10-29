package ro.utcn.sportbuddy.friends.dto;

import jakarta.validation.constraints.NotNull;

public record SendFriendRequest(@NotNull(message = "Alege utilizatorul.") Long userId) {}
