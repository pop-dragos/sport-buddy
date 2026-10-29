package ro.utcn.sportbuddy.feed.seed;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.catalog.dto.SportDto;
import ro.utcn.sportbuddy.catalog.service.CatalogService;
import ro.utcn.sportbuddy.feed.model.Comment;
import ro.utcn.sportbuddy.feed.model.Post;
import ro.utcn.sportbuddy.feed.model.PostLike;
import ro.utcn.sportbuddy.feed.model.PostTag;
import ro.utcn.sportbuddy.feed.repository.CommentRepository;
import ro.utcn.sportbuddy.feed.repository.PostLikeRepository;
import ro.utcn.sportbuddy.feed.repository.PostRepository;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Rulează după ce aplicația a pornit, deci după seeder-ele de utilizatori și catalog.
@Component
@Profile("dev")
public class FeedSeeder {

    private record Demo(String author, String sportCode, PostTag tag, int hoursAgo, String text,
                        List<String> likedBy, List<String[]> comments) {}

    private static final List<Demo> DEMO = List.of(
            new Demo("andrei", "football", PostTag.LOOKING_FOR_PLAYERS, 3,
                    "Mai căutăm doi jucători pentru sâmbătă seara, teren sintetic. Nivel mediu.",
                    List.of("ana", "vlad", "radu"),
                    List.of(new String[]{"vlad", "Eu sunt disponibil. La ce oră?"},
                            new String[]{"andrei", "La 19. Îți trimit detaliile în lobby."})),
            new Demo("ioana", "tennis", PostTag.DISCUSSION, 7,
                    "Știe cineva un teren de tenis cu nocturnă deschis și iarna?",
                    List.of("elena"),
                    List.<String[]>of(new String[]{"elena", "Clubul din parc are teren acoperit din noiembrie."})),
            new Demo("radu", "basketball", PostTag.TOURNAMENT, 26,
                    "Turneu 3x3 în weekendul viitor, echipe de câte patru. Înscrierile se fac până joi.",
                    List.of("ana", "mihai", "bogdan", "stefan"),
                    List.of()),
            new Demo("elena", "volleyball", PostTag.LOOKING_FOR_PLAYERS, 30,
                    "Avem nevoie de încă o persoană pentru volei în sală, marți de la 18.",
                    List.of("cristina"),
                    List.of()),
            new Demo("mihai", "running", PostTag.DISCUSSION, 50,
                    "Cine vine la alergare duminică dimineața în jurul lacului? Ritm lejer, cam 8 km.",
                    List.of("diana", "maria"),
                    List.<String[]>of(new String[]{"diana", "Vin. Ne vedem la intrarea principală."})),
            new Demo("bogdan", "padel", PostTag.DISCUSSION, 75,
                    "Prima dată la padel săptămâna trecută. Merită încercat, se învață repede.",
                    List.of(),
                    List.of())
    );

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository likeRepository;
    private final UserService userService;
    private final CatalogService catalogService;

    public FeedSeeder(PostRepository postRepository, CommentRepository commentRepository,
                      PostLikeRepository likeRepository, UserService userService,
                      CatalogService catalogService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userService = userService;
        this.catalogService = catalogService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (postRepository.count() > 0) return;

        Map<String, Long> sports = catalogService.getSports().stream()
                .collect(Collectors.toMap(SportDto::code, SportDto::id));
        Instant now = Instant.now();

        for (Demo d : DEMO) {
            Optional<Long> author = userService.findIdByUsername(d.author());
            Long sportId = sports.get(d.sportCode());
            if (author.isEmpty() || sportId == null) continue;

            Post post = new Post(author.get(), sportId, d.tag(), d.text());
            post.setCreatedAt(now.minus(Duration.ofHours(d.hoursAgo())));
            postRepository.save(post);

            for (String username : d.likedBy()) {
                userService.findIdByUsername(username)
                        .ifPresent(id -> likeRepository.save(new PostLike(post.getId(), id)));
            }
            for (String[] c : d.comments()) {
                userService.findIdByUsername(c[0])
                        .ifPresent(id -> commentRepository.save(new Comment(post.getId(), id, c[1])));
            }
        }
    }
}
