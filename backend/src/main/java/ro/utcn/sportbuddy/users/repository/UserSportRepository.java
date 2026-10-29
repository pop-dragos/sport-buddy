package ro.utcn.sportbuddy.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.users.model.UserSport;

import java.util.Collection;
import java.util.List;

public interface UserSportRepository extends JpaRepository<UserSport, Long> {
    List<UserSport> findByUserId(Long userId);
    List<UserSport> findByUserIdIn(Collection<Long> userIds);
    List<UserSport> findBySportIdIn(Collection<Long> sportIds);
    void deleteByUserId(Long userId);
}
