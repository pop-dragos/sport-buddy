package ro.utcn.sportbuddy.friends.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ro.utcn.sportbuddy.friends.model.DirectMessage;

import java.time.Instant;
import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    // cele mai noi primele (pentru „ultimele N”); serviciul le întoarce apoi în ordine cronologică
    @Query("""
            select m from DirectMessage m
            where (m.senderId = :a and m.recipientId = :b) or (m.senderId = :b and m.recipientId = :a)
            order by m.id desc""")
    List<DirectMessage> findLatest(Long a, Long b, Pageable page);

    // pentru interogarea la 3 secunde: doar mesajele cu id mai mare decât ultimul primit
    @Query("""
            select m from DirectMessage m
            where ((m.senderId = :a and m.recipientId = :b) or (m.senderId = :b and m.recipientId = :a))
              and m.id > :afterId
            order by m.id asc""")
    List<DirectMessage> findAfter(Long a, Long b, Long afterId);

    // cine mi-a scris și câte mesaje necitite am de la fiecare: rânduri [senderId, număr]
    @Query("""
            select m.senderId, count(m) from DirectMessage m
            where m.recipientId = :me and m.readAt is null
            group by m.senderId""")
    List<Object[]> countUnreadBySender(Long me);

    @Modifying
    @Query("""
            update DirectMessage m set m.readAt = :now
            where m.senderId = :from and m.recipientId = :to and m.readAt is null""")
    int markRead(Long from, Long to, Instant now);
}
