package ro.utcn.sportbuddy.lobbies.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

// Jobul care confirmă lobby-urile la termen și le marchează încheiate după meci.
// @EnableScheduling stă aici, în modulul lobbies, ca să nu modificăm SportbuddyApplication (fișier comun).
@Configuration
@EnableScheduling
public class LobbyScheduler {

    private final LobbyService lobbyService;

    public LobbyScheduler(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @Scheduled(fixedRate = 60_000)
    public void processDeadlines() {
        lobbyService.processDeadlines();
    }
}
