package ro.utcn.sportbuddy.payments.provider;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

// Plată simulată. Cardul 4000 0000 0000 0002 e refuzat, ca și orice număr care nu are 13–19 cifre;
// orice alt card trece (de exemplu cardul de test 4242 4242 4242 4242).
@Component
public class MockPaymentProvider implements PaymentProvider {

    static final String DECLINED_CARD = "4000000000000002";

    @Override
    public String name() {
        return "MOCK";
    }

    @Override
    public PaymentResult charge(BigDecimal amount, String currency, String cardNumber) {
        String digits = cardNumber.replaceAll("\\s", "");
        boolean valid = digits.matches("\\d{13,19}");
        if (!valid || digits.equals(DECLINED_CARD)) {
            return new PaymentResult(false, null);
        }
        return new PaymentResult(true, "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }
}
