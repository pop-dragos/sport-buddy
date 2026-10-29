package ro.utcn.sportbuddy.lobbies.dto;

import java.time.LocalDateTime;

// Ce are nevoie modulul payments despre un lobby, ca să verifice plata și să afișeze istoricul.
// finalPrice e în lei și gol cât lobby-ul nu e confirmat sau dacă terenul e gratuit.
public record PaymentTarget(Long lobbyId, String status, Integer finalPrice, boolean participant, boolean paid,
                            String sportName, String fieldName, LocalDateTime dateTime) {}
