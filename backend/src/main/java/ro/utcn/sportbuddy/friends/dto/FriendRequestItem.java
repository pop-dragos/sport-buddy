package ro.utcn.sportbuddy.friends.dto;

import java.time.LocalDateTime;

public record FriendRequestItem(Long requestId, FriendItem user, LocalDateTime createdAt) {}
