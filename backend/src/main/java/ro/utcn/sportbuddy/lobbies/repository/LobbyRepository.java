package ro.utcn.sportbuddy.lobbies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.lobbies.model.Lobby;
import ro.utcn.sportbuddy.lobbies.model.LobbyStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface LobbyRepository extends JpaRepository<Lobby, Long> {
    // Lobby-urile dintr-un interval: o zi (lista din calendar) sau o lună (punctele din calendar)
    List<Lobby> findByCityIdAndSportIdAndStartAtGreaterThanEqualAndStartAtLessThanOrderByStartAtAsc(
            Long cityId, Long sportId, LocalDateTime from, LocalDateTime to);

    // pentru job: lobby-urile cu termenul de confirmare trecut
    List<Lobby> findByStatusAndLockDeadlineLessThanEqual(LobbyStatus status, LocalDateTime time);

    // pentru job: lobby-urile al căror meci a început înainte de `time`
    List<Lobby> findByStatusAndStartAtLessThanEqual(LobbyStatus status, LocalDateTime time);
}
