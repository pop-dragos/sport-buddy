package ro.utcn.sportbuddy.lobbies.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.lobbies.dto.AttendanceRequest;
import ro.utcn.sportbuddy.lobbies.dto.CalendarDays;
import ro.utcn.sportbuddy.lobbies.dto.CreateLobbyRequest;
import ro.utcn.sportbuddy.lobbies.dto.JoinRequestResult;
import ro.utcn.sportbuddy.lobbies.dto.LeaveResult;
import ro.utcn.sportbuddy.lobbies.dto.LobbyDetails;
import ro.utcn.sportbuddy.lobbies.dto.LobbySummary;
import ro.utcn.sportbuddy.lobbies.dto.TodoCounts;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lobbies")
public class LobbyController {

    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LobbyDetails create(@Valid @RequestBody CreateLobbyRequest req) {
        return lobbyService.create(CurrentUser.id(), req);
    }

    @GetMapping
    public List<LobbySummary> listForDay(@RequestParam Long cityId,
                                         @RequestParam Long sportId,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return lobbyService.listForDay(cityId, sportId, date, CurrentUser.id());
    }

    @GetMapping("/calendar")
    public CalendarDays calendar(@RequestParam Long cityId,
                                 @RequestParam Long sportId,
                                 @RequestParam String month) {
        return lobbyService.calendar(cityId, sportId, month);
    }

    @GetMapping("/mine")
    public List<LobbySummary> mine(@RequestParam(defaultValue = "UPCOMING") String filter) {
        return lobbyService.myLobbies(CurrentUser.id(), filter);
    }

    @GetMapping("/mine/todo")
    public TodoCounts todo() {
        return lobbyService.todo(CurrentUser.id());
    }

    @GetMapping("/{id}")
    public LobbyDetails details(@PathVariable Long id) {
        return lobbyService.getDetails(id, CurrentUser.id());
    }

    @PostMapping("/{id}/join-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public JoinRequestResult requestToJoin(@PathVariable Long id) {
        return lobbyService.requestToJoin(id, CurrentUser.id());
    }

    @PostMapping("/{id}/join-requests/{requestId}/accept")
    public LobbyDetails accept(@PathVariable Long id, @PathVariable Long requestId) {
        return lobbyService.acceptRequest(id, requestId, CurrentUser.id());
    }

    @PostMapping("/{id}/join-requests/{requestId}/reject")
    public LobbyDetails reject(@PathVariable Long id, @PathVariable Long requestId) {
        return lobbyService.rejectRequest(id, requestId, CurrentUser.id());
    }

    @PostMapping("/{id}/leave")
    public LeaveResult leave(@PathVariable Long id) {
        return lobbyService.leave(id, CurrentUser.id());
    }

    @PostMapping("/{id}/lock")
    public LobbyDetails lock(@PathVariable Long id) {
        return lobbyService.lock(id, CurrentUser.id());
    }

    @PostMapping("/{id}/cancel")
    public LeaveResult cancel(@PathVariable Long id) {
        return lobbyService.cancel(id, CurrentUser.id());
    }

    @PostMapping("/{id}/attendance")
    public LobbyDetails attendance(@PathVariable Long id, @Valid @RequestBody AttendanceRequest req) {
        return lobbyService.saveAttendance(id, CurrentUser.id(), req);
    }
}
