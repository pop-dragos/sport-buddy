package ro.utcn.sportbuddy.inbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ro.utcn.sportbuddy.chat.model.LobbyMessage;

import java.util.Collection;
import java.util.List;

// Interogare de citire peste lobby_messages (chat-ul lobby-urilor), pentru lista de conversații.
public interface InboxLobbyRepository extends JpaRepository<LobbyMessage, Long> {

    // ultimul mesaj din chat-ul fiecărui lobby dat; lobby-urile fără mesaje nu apar
    @Query("""
            select m from LobbyMessage m
            where m.id in (
                select max(x.id) from LobbyMessage x
                where x.lobbyId in :lobbyIds
                group by x.lobbyId)
            order by m.id desc""")
    List<LobbyMessage> findLastPerLobby(Collection<Long> lobbyIds);
}
