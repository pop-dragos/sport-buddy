package ro.utcn.sportbuddy.lobbies.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.lobbies.model.JoinRequest;
import ro.utcn.sportbuddy.lobbies.model.JoinRequestStatus;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findByLobbyIdAndStatusOrderByCreatedAtAsc(Long lobbyId, JoinRequestStatus status);

    Optional<JoinRequest> findByLobbyIdAndUserIdAndStatus(Long lobbyId, Long userId, JoinRequestStatus status);

    // cea mai nouă cerere a unui utilizator într-un lobby, pentru myState
    Optional<JoinRequest> findFirstByLobbyIdAndUserIdOrderByCreatedAtDesc(Long lobbyId, Long userId);
}
