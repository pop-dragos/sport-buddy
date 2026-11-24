package ro.utcn.sportbuddy.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.chat.model.LobbyChatRead;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LobbyChatReadRepository extends JpaRepository<LobbyChatRead, Long> {

    Optional<LobbyChatRead> findByLobbyIdAndUserId(Long lobbyId, Long userId);

    List<LobbyChatRead> findByUserIdAndLobbyIdIn(Long userId, Collection<Long> lobbyIds);
}
