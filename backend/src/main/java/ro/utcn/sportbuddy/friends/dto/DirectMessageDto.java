package ro.utcn.sportbuddy.friends.dto;

import java.time.LocalDateTime;

public record DirectMessageDto(Long id, Long senderId, String message, LocalDateTime sentAt) {}
