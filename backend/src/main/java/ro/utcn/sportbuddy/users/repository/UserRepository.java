package ro.utcn.sportbuddy.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.users.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);
}