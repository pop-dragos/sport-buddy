package ro.utcn.sportbuddy.ratings.service;

import org.springframework.stereotype.Component;

// Formulele pentru rating și fiabilitate. Nu știe de baza de date, ca să poată fi testată direct.
@Component
public class RatingCalculator {

    public static final int MIN_STARS = 1;
    public static final int MAX_STARS = 5;

    // Penalizările sunt review-uri automate cu aceste valori.
    public static final int LATE_CANCEL_STARS = 2;
    public static final int NO_SHOW_STARS = 1;

    // mediaNouă = (media * n + v) / (n + 1); fără review-uri, media devine direct v.
    public double addReview(Double currentAverage, int reviewCount, int stars) {
        if (stars < MIN_STARS || stars > MAX_STARS) {
            throw new IllegalArgumentException("Nota trebuie să fie între 1 și 5.");
        }
        if (reviewCount < 0) {
            throw new IllegalArgumentException("Numărul de review-uri nu poate fi negativ.");
        }
        if (reviewCount == 0 || currentAverage == null) {
            return stars;
        }
        return (currentAverage * reviewCount + stars) / (reviewCount + 1);
    }

    // prezențe / (prezențe + 0.5 * anulăriTârzii + absențe), în procente rotunjite.
    // Întoarce null pentru cine nu are istoric (interfața afișează „nou").
    public Integer reliabilityPercent(int attended, int lateCancels, int noShows) {
        if (attended < 0 || lateCancels < 0 || noShows < 0) {
            throw new IllegalArgumentException("Contoarele nu pot fi negative.");
        }
        double total = attended + 0.5 * lateCancels + noShows;
        if (total == 0) {
            return null;
        }
        return (int) Math.round(attended * 100.0 / total);
    }
}
