package ro.utcn.sportbuddy.lobbies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.lobbies.model.LobbyPlayer;
import ro.utcn.sportbuddy.lobbies.model.PlayerStatus;

import java.util.List;
import java.util.Optional;

public interface LobbyPlayerRepository extends JpaRepository<LobbyPlayer, Long> {
    List<LobbyPlayer> findByLobbyIdAndStatus(Long lobbyId, PlayerStatus status);

    long countByLobbyIdAndStatus(Long lobbyId, PlayerStatus status);

    Optional<LobbyPlayer> findByLobbyIdAndUserId(Long lobbyId, Long userId);
}
