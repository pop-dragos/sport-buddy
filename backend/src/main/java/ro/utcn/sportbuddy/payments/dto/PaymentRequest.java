package ro.utcn.sportbuddy.payments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Datele cardului sunt folosite doar pentru încasare și nu se salvează.
public record PaymentRequest(
        @NotNull(message = "Alege lobby-ul.") Long lobbyId,
        @NotBlank(message = "Completează numărul cardului.") String cardNumber,
        @NotBlank(message = "Completează data expirării.") String expiry,
        @NotBlank(message = "Completează codul CVC.") String cvc) {}
