package ro.utcn.sportbuddy.payments.provider;

import java.math.BigDecimal;

// Cine încasează efectiv banii. Acum e MockPaymentProvider; un provider real (de exemplu Stripe)
// ar implementa aceeași interfață, fără să schimbăm PaymentService.
public interface PaymentProvider {

    String name();

    PaymentResult charge(BigDecimal amount, String currency, String cardNumber);

    record PaymentResult(boolean success, String reference) {}
}
