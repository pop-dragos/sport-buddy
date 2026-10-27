package ro.utcn.sportbuddy.lobbies.dto;

import java.time.LocalDateTime;

// Un lobby într-o listă (calendar, agendă, profil). Forma din docs/api.md.
public record LobbySummary(
        Long id,
        Long sportId,
        String sportCode,
        String sportName,
        Long fieldId,
        String fieldName,
        Long cityId,
        String cityName,
        LocalDateTime dateTime,
        String status,
        int maxPlayers,
        int currentPlayers,
        int freeSlots,
        Double averageRating,
        String myState) {}
