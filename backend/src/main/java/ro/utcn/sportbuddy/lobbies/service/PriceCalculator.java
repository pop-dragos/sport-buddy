package ro.utcn.sportbuddy.lobbies.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Prețul pe jucător. Nu știe de baza de date, ca să poată fi testat direct.
@Component
public class PriceCalculator {

    // Locuri ocupate = jucătorii aduși de organizator (fără cont) + jucătorii cu cont din lobby.
    public int occupiedSlots(int guestCount, long joinedPlayers) {
        if (guestCount < 0 || joinedPlayers < 0) {
            throw new IllegalArgumentException("Numărul de jucători nu poate fi negativ.");
        }
        return guestCount + (int) joinedPlayers;
    }

    // ceil(costTeren / jucători), în lei întregi. Întoarce null pentru terenurile gratuite.
    public BigDecimal pricePerPlayer(BigDecimal fieldCost, int players) {
        if (fieldCost.signum() < 0) {
            throw new IllegalArgumentException("Costul terenului nu poate fi negativ.");
        }
        if (players <= 0) {
            throw new IllegalArgumentException("Trebuie să existe cel puțin un jucător.");
        }
        if (fieldCost.signum() == 0) {
            return null;
        }
        return fieldCost.divide(BigDecimal.valueOf(players), 0, RoundingMode.CEILING);
    }
}
