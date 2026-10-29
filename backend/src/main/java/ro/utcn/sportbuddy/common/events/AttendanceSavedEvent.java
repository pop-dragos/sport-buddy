package ro.utcn.sportbuddy.common.events;

import java.util.List;

// Organizatorul a bifat cine a venit la meci.
public record AttendanceSavedEvent(Long lobbyId, Long hostUserId, List<Long> presentUserIds,
                                   List<Long> absentUserIds) {}
