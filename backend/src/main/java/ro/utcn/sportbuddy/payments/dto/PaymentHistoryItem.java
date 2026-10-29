package ro.utcn.sportbuddy.payments.dto;

import java.time.Instant;
import java.time.LocalDateTime;

// Un rând din istoricul plăților (Setări > Plăți).
public record PaymentHistoryItem(Long paymentId, Long lobbyId, String sportName, String fieldName,
                                 LocalDateTime dateTime, int amount, String status, Instant createdAt) {}
