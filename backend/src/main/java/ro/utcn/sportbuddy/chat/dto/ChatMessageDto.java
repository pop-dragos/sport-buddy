package ro.utcn.sportbuddy.chat.dto;

import java.time.LocalDateTime;

// type este USER sau SYSTEM; la SYSTEM, senderId și senderUsername sunt goale.
public record ChatMessageDto(Long id, String type, Long senderId, String senderUsername,
                             String message, LocalDateTime sentAt) {
}
