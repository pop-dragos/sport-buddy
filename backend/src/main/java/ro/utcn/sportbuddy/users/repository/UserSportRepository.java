package ro.utcn.sportbuddy.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.users.model.UserSport;

import java.util.List;

public interface UserSportRepository extends JpaRepository<UserSport, Long> {
    List<UserSport> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}