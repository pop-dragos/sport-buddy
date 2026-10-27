package ro.utcn.sportbuddy.lobbies.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.lobbies.dto.CalendarDays;
import ro.utcn.sportbuddy.lobbies.dto.CreateLobbyRequest;
import ro.utcn.sportbuddy.lobbies.dto.LobbyDetails;
import ro.utcn.sportbuddy.lobbies.dto.LobbySummary;
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

    @GetMapping("/{id}")
    public LobbyDetails details(@PathVariable Long id) {
        return lobbyService.getDetails(id, CurrentUser.id());
    }
}
