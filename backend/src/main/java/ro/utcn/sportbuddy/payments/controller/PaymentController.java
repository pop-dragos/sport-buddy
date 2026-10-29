package ro.utcn.sportbuddy.payments.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.payments.dto.PaymentHistoryItem;
import ro.utcn.sportbuddy.payments.dto.PaymentRequest;
import ro.utcn.sportbuddy.payments.dto.PaymentResponse;
import ro.utcn.sportbuddy.payments.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse pay(@Valid @RequestBody PaymentRequest req) {
        return paymentService.pay(CurrentUser.id(), req);
    }

    @GetMapping("/history")
    public List<PaymentHistoryItem> history() {
        return paymentService.history(CurrentUser.id());
    }
}
