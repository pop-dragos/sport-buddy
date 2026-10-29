package ro.utcn.sportbuddy.lobbies.controller;

import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.lobbies.dto.LobbyDetails;
import ro.utcn.sportbuddy.lobbies.dto.SimulateCancelRequest;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;

// Simulare pentru demo și testare, ca să nu așteptăm ore întregi termenul sau meciul. Doar pe profilul dev.
@RestController
@Profile("dev")
@RequestMapping("/api/dev/lobbies")
public class DevLobbyController {

    private final LobbyService lobbyService;

    public DevLobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping("/{id}/simulate-join")
    public LobbyDetails simulateJoin(@PathVariable Long id) {
        return lobbyService.simulateJoin(id, CurrentUser.id());
    }

    @PostMapping("/{id}/simulate-cancel")
    public LobbyDetails simulateCancel(@PathVariable Long id, @Valid @RequestBody SimulateCancelRequest req) {
        return lobbyService.simulateCancel(id, CurrentUser.id(), req.userId());
    }

    @PostMapping("/{id}/pass-deadline")
    public LobbyDetails passDeadline(@PathVariable Long id) {
        return lobbyService.simulatePassDeadline(id, CurrentUser.id());
    }

    @PostMapping("/{id}/finish")
    public LobbyDetails finish(@PathVariable Long id) {
        return lobbyService.simulateFinish(id, CurrentUser.id());
    }
}
