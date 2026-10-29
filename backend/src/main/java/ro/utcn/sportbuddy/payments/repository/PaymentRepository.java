package ro.utcn.sportbuddy.payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.payments.model.Payment;
import ro.utcn.sportbuddy.payments.model.PaymentStatus;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByLobbyIdAndUserIdAndStatus(Long lobbyId, Long userId, PaymentStatus status);
}
