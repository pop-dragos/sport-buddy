package ro.utcn.sportbuddy.payments.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.lobbies.dto.PaymentTarget;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.payments.dto.PaymentRequest;
import ro.utcn.sportbuddy.payments.dto.PaymentResponse;
import ro.utcn.sportbuddy.payments.model.Payment;
import ro.utcn.sportbuddy.payments.model.PaymentStatus;
import ro.utcn.sportbuddy.payments.provider.MockPaymentProvider;
import ro.utcn.sportbuddy.payments.repository.PaymentRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// LobbyService și repository-ul sunt simulate (Mockito), deci testul nu are nevoie de bază de date.
class PaymentServiceTest {

    private static final Long LOBBY = 55L;
    private static final Long ANA = 1L;
    private static final String GOOD_CARD = "4242 4242 4242 4242";
    private static final String BAD_CARD = "4000 0000 0000 0002";

    private PaymentRepository paymentRepository;
    private LobbyService lobbyService;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        lobbyService = mock(LobbyService.class);
        paymentService = new PaymentService(paymentRepository, new MockPaymentProvider(), lobbyService);
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private void lobby(String status, Integer finalPrice, boolean participant, boolean paid) {
        when(lobbyService.paymentTarget(LOBBY, ANA)).thenReturn(new PaymentTarget(LOBBY, status, finalPrice,
                participant, paid, "Fotbal", "Teren de fotbal – Centru", LocalDateTime.of(2026, 11, 5, 18, 0)));
    }

    private ApiException payFails(String card) {
        return assertThrows(ApiException.class, () -> paymentService.pay(ANA, new PaymentRequest(LOBBY, card, "12/29", "123")));
    }

    @Test
    void successfulPaymentMarksPlayerAsPaidOnline() {
        lobby("LOCKED", 34, true, false);

        PaymentResponse response = paymentService.pay(ANA, new PaymentRequest(LOBBY, GOOD_CARD, "12/29", "123"));

        assertEquals("PAID", response.status());
        assertEquals(34, response.amount());
        verify(lobbyService).markPaidOnline(LOBBY, ANA);
    }

    @Test
    void declinedCardIsSavedAsFailedAndNotMarkedPaid() {
        lobby("LOCKED", 34, true, false);

        assertEquals("CARD_DECLINED", payFails(BAD_CARD).getCode());

        verify(paymentRepository).save(argThat((Payment p) -> p.getStatus() == PaymentStatus.FAILED));
        verify(lobbyService, never()).markPaidOnline(any(), any());
    }

    @Test
    void openLobbyCannotBePaid() {
        lobby("OPEN", null, true, false);
        assertEquals("LOBBY_NOT_LOCKED", payFails(GOOD_CARD).getCode());
    }

    @Test
    void onlyParticipantsCanPay() {
        lobby("LOCKED", 34, false, false);
        assertEquals("NOT_PARTICIPANT", payFails(GOOD_CARD).getCode());
    }

    @Test
    void cannotPayTwice() {
        lobby("LOCKED", 34, true, true);
        assertEquals("ALREADY_PAID", payFails(GOOD_CARD).getCode());
    }

    @Test
    void freeLobbyHasNothingToPay() {
        lobby("LOCKED", null, true, false);
        assertEquals("NOTHING_TO_PAY", payFails(GOOD_CARD).getCode());
    }
}
