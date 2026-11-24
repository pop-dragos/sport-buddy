package ro.utcn.sportbuddy.notifications.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.notifications.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // cele mai noi primele
    List<Notification> findTop50ByUserIdOrderByIdDesc(Long userId);

    long countByUserIdAndReadAtIsNull(Long userId);

    List<Notification> findByUserIdAndReadAtIsNull(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
