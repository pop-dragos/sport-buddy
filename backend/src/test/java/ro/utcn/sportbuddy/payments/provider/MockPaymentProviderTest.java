package ro.utcn.sportbuddy.payments.provider;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test unitar simplu: nu pornește Spring și nu are nevoie de PostgreSQL.
class MockPaymentProviderTest {

    private static final BigDecimal AMOUNT = new BigDecimal("50");

    private final MockPaymentProvider provider = new MockPaymentProvider();

    @Test
    void testCardIsAccepted() {
        PaymentProvider.PaymentResult result = provider.charge(AMOUNT, "RON", "4242 4242 4242 4242");
        assertTrue(result.success());
        assertNotNull(result.reference());
    }

    @Test
    void declinedTestCardIsRefused() {
        assertFalse(provider.charge(AMOUNT, "RON", "4000 0000 0000 0002").success());
    }

    @Test
    void invalidNumbersAreRefused() {
        assertFalse(provider.charge(AMOUNT, "RON", "1234").success());
        assertFalse(provider.charge(AMOUNT, "RON", "abcd efgh ijkl mnop").success());
    }
}
