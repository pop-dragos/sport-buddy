package ro.utcn.sportbuddy.feed.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.catalog.service.CatalogService;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.feed.dto.CommentDto;
import ro.utcn.sportbuddy.feed.dto.LikeResult;
import ro.utcn.sportbuddy.feed.dto.PostDto;
import ro.utcn.sportbuddy.feed.model.Comment;
import ro.utcn.sportbuddy.feed.model.Post;
import ro.utcn.sportbuddy.feed.model.PostLike;
import ro.utcn.sportbuddy.feed.model.PostTag;
import ro.utcn.sportbuddy.feed.repository.CommentRepository;
import ro.utcn.sportbuddy.feed.repository.PostLikeRepository;
import ro.utcn.sportbuddy.feed.repository.PostRepository;
import ro.utcn.sportbuddy.ratings.service.RatingService;
import ro.utcn.sportbuddy.users.dto.UserRef;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class FeedService {

    public static final int MAX_PAGE_SIZE = 50;
    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository likeRepository;
    private final UserService userService;
    private final CatalogService catalogService;
    private final RatingService ratingService;

    public FeedService(PostRepository postRepository, CommentRepository commentRepository,
                       PostLikeRepository likeRepository, UserService userService,
                       CatalogService catalogService, RatingService ratingService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userService = userService;
        this.catalogService = catalogService;
        this.ratingService = ratingService;
    }

    // Cele mai noi primele. Fără sportId: toate sporturile.
    @Transactional(readOnly = true)
    public List<PostDto> getFeed(Long viewerId, Long sportId, int page, int size) {
        if (page < 0) throw invalid("page", "Pagina nu poate fi negativă.");
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw invalid("size", "Mărimea paginii trebuie să fie între 1 și " + MAX_PAGE_SIZE + ".");
        }
        PageRequest request = PageRequest.of(page, size);
        List<Post> posts = sportId == null
                ? postRepository.findAllByOrderByCreatedAtDescIdDesc(request)
                : postRepository.findBySportIdOrderByCreatedAtDescIdDesc(sportId, request);
        return toDtos(viewerId, posts);
    }

    @Transactional
    public PostDto createPost(Long authorId, String content, Long sportId, PostTag tag) {
        if (catalogService.findSport(sportId).isEmpty()) {
            throw invalid("sportId", "Sportul ales nu există.");
        }
        Post post = postRepository.save(new Post(authorId, sportId, tag, clean(content, "content")));
        return toDtos(authorId, List.of(post)).getFirst();
    }

    // Comutator: a doua apăsare scoate like-ul.
    @Transactional
    public LikeResult toggleLike(Long userId, Long postId) {
        requirePost(postId);
        Optional<PostLike> existing = likeRepository.findByPostIdAndUserId(postId, userId);
        existing.ifPresentOrElse(likeRepository::delete,
                () -> likeRepository.save(new PostLike(postId, userId)));
        return new LikeResult(existing.isEmpty(), likeRepository.countByPostId(postId));
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long postId) {
        requirePost(postId);
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAscIdAsc(postId);
        Map<Long, UserRef> authors = userService.findRefs(
                comments.stream().map(Comment::getAuthorId).distinct().toList());
        return comments.stream().map(c -> toDto(c, authors)).toList();
    }

    @Transactional
    public CommentDto addComment(Long authorId, Long postId, String content) {
        requirePost(postId);
        Comment comment = commentRepository.save(new Comment(postId, authorId, clean(content, "content")));
        return toDto(comment, userService.findRefs(List.of(authorId)));
    }

    // ---------- helperi ----------

    private void requirePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Postarea nu există.");
        }
    }

    private static String clean(String text, String field) {
        if (text == null || text.isBlank()) throw invalid(field, "Textul nu poate fi gol.");
        return text.trim();
    }

    private static ApiException invalid(String field, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.", Map.of(field, message));
    }

    // Numărătorile și like-urile mele se iau pentru toată pagina deodată, nu câte o interogare pe postare.
    private List<PostDto> toDtos(Long viewerId, List<Post> posts) {
        if (posts.isEmpty()) return List.of();
        List<Long> ids = posts.stream().map(Post::getId).toList();
        Map<Long, UserRef> authors = userService.findRefs(posts.stream().map(Post::getAuthorId).distinct().toList());
        Map<Long, Long> likes = toCounts(likeRepository.countByPostIds(ids));
        Map<Long, Long> comments = toCounts(commentRepository.countByPostIds(ids));
        Set<Long> likedByMe = new HashSet<>(likeRepository.findLikedPostIds(viewerId, ids));
        // o singură cerere de rating pentru fiecare autor, chiar dacă are mai multe postări pe pagină
        Map<Long, Double> ratings = new HashMap<>();
        for (Long authorId : authors.keySet()) ratings.put(authorId, ratingService.getStats(authorId).average());

        return posts.stream().map(p -> new PostDto(p.getId(), author(authors, p.getAuthorId()), ratings.get(p.getAuthorId()), p.getSportId(),
                p.getTag(), p.getText(), local(p.getCreatedAt()), likes.getOrDefault(p.getId(), 0L),
                likedByMe.contains(p.getId()), comments.getOrDefault(p.getId(), 0L))).toList();
    }

    private static CommentDto toDto(Comment c, Map<Long, UserRef> authors) {
        return new CommentDto(c.getId(), author(authors, c.getAuthorId()), c.getText(), local(c.getCreatedAt()));
    }

    private static UserRef author(Map<Long, UserRef> authors, Long id) {
        return authors.getOrDefault(id, new UserRef(id, "necunoscut", "Utilizator necunoscut", null));
    }

    private static Map<Long, Long> toCounts(List<Object[]> rows) {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : rows) counts.put((Long) row[0], (Long) row[1]);
        return counts;
    }

    private static LocalDateTime local(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZONE);
    }
}
