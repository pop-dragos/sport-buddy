package ro.utcn.sportbuddy.lobbies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.lobbies.model.LobbyPlayer;
import ro.utcn.sportbuddy.lobbies.model.PlayerStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LobbyPlayerRepository extends JpaRepository<LobbyPlayer, Long> {
    List<LobbyPlayer> findByLobbyIdAndStatus(Long lobbyId, PlayerStatus status);

    long countByLobbyIdAndStatus(Long lobbyId, PlayerStatus status);

    // „In" = statusul e unul din listă (SQL: status IN (...))
    List<LobbyPlayer> findByLobbyIdAndStatusIn(Long lobbyId, Collection<PlayerStatus> statuses);

    long countByLobbyIdAndStatusIn(Long lobbyId, Collection<PlayerStatus> statuses);

    // pentru agendă: lobby-urile în care joacă un utilizator
    List<LobbyPlayer> findByUserIdAndStatusIn(Long userId, Collection<PlayerStatus> statuses);

    Optional<LobbyPlayer> findByLobbyIdAndUserId(Long lobbyId, Long userId);
}
