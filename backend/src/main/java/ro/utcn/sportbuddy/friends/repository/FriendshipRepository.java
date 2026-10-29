package ro.utcn.sportbuddy.friends.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ro.utcn.sportbuddy.friends.model.Friendship;
import ro.utcn.sportbuddy.friends.model.FriendshipStatus;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // relația dintre doi utilizatori, indiferent cine a trimis cererea
    @Query("""
            select f from Friendship f
            where (f.requesterId = :a and f.addresseeId = :b) or (f.requesterId = :b and f.addresseeId = :a)""")
    List<Friendship> findBetween(Long a, Long b);

    @Query("select f from Friendship f where f.requesterId = :userId or f.addresseeId = :userId")
    List<Friendship> findInvolving(Long userId);

    @Query("""
            select f from Friendship f
            where f.status = ro.utcn.sportbuddy.friends.model.FriendshipStatus.ACCEPTED
              and (f.requesterId = :userId or f.addresseeId = :userId)""")
    List<Friendship> findAccepted(Long userId);

    List<Friendship> findByAddresseeIdAndStatusOrderByCreatedAtDesc(Long addresseeId, FriendshipStatus status);

    List<Friendship> findByRequesterIdAndStatusOrderByCreatedAtDesc(Long requesterId, FriendshipStatus status);
}
