package ro.utcn.sportbuddy.lobbies.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.lobbies.dto.CreateLobbyRequest;
import ro.utcn.sportbuddy.lobbies.dto.LobbyDetails;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;

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

    @GetMapping("/{id}")
    public LobbyDetails details(@PathVariable Long id) {
        return lobbyService.getDetails(id, CurrentUser.id());
    }
}
