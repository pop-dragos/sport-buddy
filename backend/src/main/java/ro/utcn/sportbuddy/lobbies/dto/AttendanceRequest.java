package ro.utcn.sportbuddy.lobbies.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// { "attendance": [ { "userId": 21, "present": true }, ... ] }, fără organizator
public record AttendanceRequest(@NotNull(message = "Lista de prezență lipsește.") List<@Valid Entry> attendance) {

    public record Entry(@NotNull Long userId, @NotNull Boolean present) {}
}
