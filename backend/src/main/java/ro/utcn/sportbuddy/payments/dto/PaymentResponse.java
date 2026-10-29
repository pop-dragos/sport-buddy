package ro.utcn.sportbuddy.payments.dto;

public record PaymentResponse(Long paymentId, String status, int amount) {}
