package ro.utcn.sportbuddy.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.chat.model.LobbyMessage;

import java.util.List;

public interface LobbyMessageRepository extends JpaRepository<LobbyMessage, Long> {

    // ultimele mesaje, de la cel mai nou la cel mai vechi (service-ul le întoarce în ordine)
    List<LobbyMessage> findTop100ByLobbyIdOrderByIdDesc(Long lobbyId);

    // doar mesajele noi, pentru interogarea la 3 secunde
    List<LobbyMessage> findByLobbyIdAndIdGreaterThanOrderByIdAsc(Long lobbyId, Long afterId);

    // mesajele scrise de alți utilizatori (fără cele de sistem) după id-ul dat: cele necitite
    long countByLobbyIdAndSystemFalseAndSenderIdNotAndIdGreaterThan(Long lobbyId, Long senderId, Long afterId);
}
