package ro.utcn.sportbuddy.ratings.seed;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ro.utcn.sportbuddy.ratings.repository.ReviewRepository;
import ro.utcn.sportbuddy.ratings.service.RatingService;
import ro.utcn.sportbuddy.users.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Review-uri de test pentru utilizatorii demo, ca fiecare să aibă alt rating și altă fiabilitate.
// Trece prin RatingService, deci cifrele se calculează exact ca în aplicația reală.
// Review-urile nu au lobby (lobbyId = null): nu există încă lobby-uri de test.
@Component
@Profile("dev")
public class RatingSeeder {

    // username, note primite, prezențe, anulări târzii, absențe; ana rămâne „nou"
    private record DemoRating(String username, int[] stars, int attended, int lateCancels, int noShows) {}

    private static final List<DemoRating> PROFILES = List.of(
            new DemoRating("ioana", new int[] {5, 5, 5, 4, 5, 5, 4, 5, 5}, 9, 0, 0),
            new DemoRating("radu", new int[] {5, 4, 5, 4, 4, 5, 4, 5, 4, 4, 5, 4, 5}, 13, 1, 0),
            new DemoRating("bogdan", new int[] {5, 4, 4, 5, 5, 4, 4, 5}, 8, 0, 0),
            new DemoRating("elena", new int[] {5, 4, 5, 5, 4}, 5, 0, 0),
            new DemoRating("vlad", new int[] {4, 5, 4, 5, 4, 4}, 6, 0, 0),
            new DemoRating("andrei", new int[] {4, 4, 3, 4, 5, 3, 4}, 7, 0, 1),
            new DemoRating("mihai", new int[] {3, 4, 3, 3, 4, 2, 3}, 6, 1, 1),
            new DemoRating("cristina", new int[] {4, 4, 5}, 3, 0, 0),
            new DemoRating("maria", new int[] {4}, 1, 0, 0)
    );

    private final ReviewRepository reviewRepository;
    private final RatingService ratingService;
    private final UserService userService;

    public RatingSeeder(ReviewRepository reviewRepository, RatingService ratingService, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.ratingService = ratingService;
        this.userService = userService;
    }

    // ApplicationReadyEvent vine după toate seeder-ele de tip CommandLineRunner, deci utilizatorii demo există deja.
    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (reviewRepository.count() > 0)
            return;

        // notele vin, pe rând, de la ceilalți utilizatori demo
        List<Long> reviewers = new ArrayList<>();
        for (DemoRating p : PROFILES) {
            userService.findIdByUsername(p.username()).ifPresent(reviewers::add);
        }
        if (reviewers.size() < 2)
            return;

        for (DemoRating p : PROFILES) {
            Optional<Long> id = userService.findIdByUsername(p.username());
            if (id.isEmpty())
                continue;
            Long userId = id.get();
            List<Long> others = reviewers.stream().filter(r -> !r.equals(userId)).toList();

            for (int i = 0; i < p.stars().length; i++) {
                ratingService.recordReview(null, others.get(i % others.size()), userId, p.stars()[i]);
            }
            for (int i = 0; i < p.attended(); i++) {
                ratingService.recordAttendance(userId);
            }
            for (int i = 0; i < p.lateCancels(); i++) {
                ratingService.applyLateCancel(null, userId);
            }
            for (int i = 0; i < p.noShows(); i++) {
                ratingService.applyNoShow(null, userId);
            }
        }
    }
}
