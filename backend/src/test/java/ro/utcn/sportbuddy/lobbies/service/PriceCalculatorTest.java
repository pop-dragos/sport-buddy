package ro.utcn.sportbuddy.lobbies.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Test unitar simplu: nu pornește Spring și nu are nevoie de PostgreSQL.
class PriceCalculatorTest {

    private final PriceCalculator calculator = new PriceCalculator();

    @Test
    void occupiedSlotsAddGuestsAndPlayers() {
        // 2 aduși de organizator + 4 jucători cu cont
        assertEquals(6, calculator.occupiedSlots(2, 4));
    }

    @Test
    void priceIsRoundedUp() {
        // 200 / 6 = 33.33 -> 34 lei (exemplul din docs/api.md)
        assertEquals(new BigDecimal("34"), calculator.pricePerPlayer(new BigDecimal("200"), 6));
    }

    @Test
    void exactDivisionIsNotRounded() {
        // 200 / 10 = 20 lei (prețul cu lobby-ul plin din docs/api.md)
        assertEquals(new BigDecimal("20"), calculator.pricePerPlayer(new BigDecimal("200"), 10));
    }

    @Test
    void freeFieldHasNoPrice() {
        assertNull(calculator.pricePerPlayer(BigDecimal.ZERO, 6));
    }

    @Test
    void invalidValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> calculator.pricePerPlayer(new BigDecimal("200"), 0));
        assertThrows(IllegalArgumentException.class, () -> calculator.pricePerPlayer(new BigDecimal("-1"), 5));
        assertThrows(IllegalArgumentException.class, () -> calculator.occupiedSlots(-1, 3));
    }
}
