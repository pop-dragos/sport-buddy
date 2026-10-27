package ro.utcn.sportbuddy.lobbies.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

// alreadyPlaying = câți jucători sunt deja, cu tot cu organizatorul
public record CreateLobbyRequest(
        @NotNull(message = "Alege sportul.") Long sportId,
        @NotNull(message = "Alege terenul.") Long fieldId,
        @NotNull(message = "Alege data și ora.") LocalDateTime dateTime,
        @NotNull(message = "Alege numărul de locuri.")
        @Min(value = 2, message = "Lobby-ul trebuie să aibă cel puțin 2 locuri.")
        @Max(value = 50, message = "Lobby-ul poate avea maximum 50 de locuri.") Integer maxPlayers,
        @NotNull(message = "Alege minimul de jucători.")
        @Min(value = 2, message = "Minimul de jucători este 2.") Integer minPlayers,
        @NotNull(message = "Spune câți jucători sunt deja.")
        @Min(value = 1, message = "Cel puțin organizatorul joacă.") Integer alreadyPlaying,
        @NotNull(message = "Completează costul terenului.")
        @Min(value = 0, message = "Costul nu poate fi negativ.") Integer fieldCost) {}
