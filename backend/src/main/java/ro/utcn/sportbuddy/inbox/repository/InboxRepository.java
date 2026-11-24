package ro.utcn.sportbuddy.inbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ro.utcn.sportbuddy.friends.model.DirectMessage;

import java.util.List;

// Interogări de citire peste tabelul direct_messages, făcute pentru lista de conversații.
// Mesajele se scriu și se marchează citite tot prin modulul friends.
public interface InboxRepository extends JpaRepository<DirectMessage, Long> {

    // ultimul mesaj din fiecare conversație a mea (cu fiecare persoană), cel mai recent primul
    @Query("""
            select m from DirectMessage m
            where m.id in (
                select max(x.id) from DirectMessage x
                where x.senderId = :me or x.recipientId = :me
                group by case when x.senderId = :me then x.recipientId else x.senderId end)
            order by m.id desc""")
    List<DirectMessage> findLastPerConversation(Long me);

    // cine mi-a scris și câte mesaje necitite am de la fiecare: rânduri [senderId, număr]
    @Query("""
            select m.senderId, count(m) from DirectMessage m
            where m.recipientId = :me and m.readAt is null
            group by m.senderId""")
    List<Object[]> countUnreadBySender(Long me);

    @Query("select count(m) from DirectMessage m where m.recipientId = :me and m.readAt is null")
    long countUnread(Long me);
}
