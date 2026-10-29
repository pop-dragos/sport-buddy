package ro.utcn.sportbuddy.payments.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.lobbies.dto.PaymentTarget;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.payments.dto.PaymentHistoryItem;
import ro.utcn.sportbuddy.payments.dto.PaymentRequest;
import ro.utcn.sportbuddy.payments.dto.PaymentResponse;
import ro.utcn.sportbuddy.payments.model.Payment;
import ro.utcn.sportbuddy.payments.model.PaymentStatus;
import ro.utcn.sportbuddy.payments.provider.PaymentProvider;
import ro.utcn.sportbuddy.payments.provider.PaymentProvider.PaymentResult;
import ro.utcn.sportbuddy.payments.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;
    private final LobbyService lobbyService;

    public PaymentService(PaymentRepository paymentRepository, PaymentProvider paymentProvider,
            LobbyService lobbyService) {
        this.paymentRepository = paymentRepository;
        this.paymentProvider = paymentProvider;
        this.lobbyService = lobbyService;
    }

    // Plata online, la prețul final, doar după confirmare și o singură dată.
    // Fără @Transactional: o plată refuzată rămâne salvată (FAILED) chiar dacă aruncăm eroarea 402.
    public PaymentResponse pay(Long userId, PaymentRequest req) {
        PaymentTarget lobby = lobbyService.paymentTarget(req.lobbyId(), userId);
        if (!lobby.participant()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_PARTICIPANT", "Nu ești în acest lobby.");
        }
        if (!"LOCKED".equals(lobby.status())) {
            throw new ApiException(HttpStatus.CONFLICT, "LOBBY_NOT_LOCKED",
                    "Plata online e posibilă după confirmarea lobby-ului.");
        }
        if (lobby.finalPrice() == null || lobby.finalPrice() <= 0) {
            throw new ApiException(HttpStatus.CONFLICT, "NOTHING_TO_PAY", "Lobby-ul este gratuit.");
        }
        if (lobby.paid() || paymentRepository.existsByLobbyIdAndUserIdAndStatus(req.lobbyId(), userId, PaymentStatus.PAID)) {
            throw new ApiException(HttpStatus.CONFLICT, "ALREADY_PAID", "Ai plătit deja acest meci.");
        }

        BigDecimal amount = BigDecimal.valueOf(lobby.finalPrice());
        PaymentResult result = paymentProvider.charge(amount, "RON", req.cardNumber());

        Payment payment = new Payment();
        payment.setLobbyId(req.lobbyId());
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setProvider(paymentProvider.name());
        payment.setProviderRef(result.reference());
        payment.setStatus(result.success() ? PaymentStatus.PAID : PaymentStatus.FAILED);
        payment = paymentRepository.save(payment);

        if (!result.success()) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "CARD_DECLINED",
                    "Plata a fost refuzată. Încearcă alt card.");
        }
        lobbyService.markPaidOnline(req.lobbyId(), userId);
        return new PaymentResponse(payment.getId(), payment.getStatus().name(), lobby.finalPrice());
    }

    public List<PaymentHistoryItem> history(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(p -> {
                    PaymentTarget lobby = lobbyService.paymentTarget(p.getLobbyId(), userId);
                    return new PaymentHistoryItem(p.getId(), p.getLobbyId(), lobby.sportName(), lobby.fieldName(),
                            lobby.dateTime(), p.getAmount().intValue(), p.getStatus().name(), p.getCreatedAt());
                })
                .toList();
    }
}
