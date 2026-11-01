package ro.utcn.sportbuddy.feed.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import ro.utcn.sportbuddy.catalog.dto.SportDto;
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
import ro.utcn.sportbuddy.ratings.dto.RatingStatsDto;
import ro.utcn.sportbuddy.ratings.service.RatingService;
import ro.utcn.sportbuddy.users.dto.UserRef;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FeedServiceTest {

    private static final Long ANA = 1L;
    private static final Long BOGDAN = 2L;

    private PostRepository posts;
    private CommentRepository comments;
    private PostLikeRepository likes;
    private UserService users;
    private CatalogService catalog;
    private RatingService ratings;
    private FeedService service;

    @BeforeEach
    void setUp() {
        posts = mock(PostRepository.class);
        comments = mock(CommentRepository.class);
        likes = mock(PostLikeRepository.class);
        users = mock(UserService.class);
        catalog = mock(CatalogService.class);
        ratings = mock(RatingService.class);
        when(ratings.getStats(any())).thenAnswer(i -> RatingStatsDto.newUser(i.getArgument(0)));
        service = new FeedService(posts, comments, likes, users, catalog, ratings);

        when(users.findRefs(any())).thenReturn(Map.of(
                ANA, new UserRef(ANA, "ana", "Ana Popescu", null),
                BOGDAN, new UserRef(BOGDAN, "bogdan", "Bogdan Pop", "/api/uploads/2-x.png")));
        when(catalog.findSport(any())).thenReturn(Optional.of(mock(SportDto.class)));
        when(posts.save(any())).thenAnswer(i -> withId(i.getArgument(0), 10L));
        when(comments.save(any())).thenAnswer(i -> {
            Comment c = i.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 50L);
            ReflectionTestUtils.setField(c, "createdAt", Instant.now());
            return c;
        });
    }

    private static Post withId(Post p, Long id) {
        ReflectionTestUtils.setField(p, "id", id);
        if (p.getCreatedAt() == null) p.setCreatedAt(Instant.now());
        return p;
    }

    private static Post post(Long id, Long author) {
        return withId(new Post(author, 1L, PostTag.DISCUSSION, "Text " + id), id);
    }

    // ---------- listare ----------

    @Test
    void feedCombinesCountsLikesAndAuthorsForThePage() {
        when(posts.findAllByOrderByCreatedAtDescIdDesc(any())).thenReturn(List.of(post(1L, ANA), post(2L, BOGDAN)));
        when(likes.countByPostIds(any())).thenReturn(List.<Object[]>of(new Object[]{1L, 3L}));
        when(comments.countByPostIds(any())).thenReturn(List.<Object[]>of(new Object[]{2L, 5L}));
        when(likes.findLikedPostIds(eq(ANA), any())).thenReturn(List.of(2L));

        List<PostDto> feed = service.getFeed(ANA, null, 0, 20);

        assertEquals(2, feed.size());
        assertEquals(3, feed.get(0).likeCount());
        assertEquals(0, feed.get(0).commentCount());
        assertFalse(feed.get(0).likedByMe());
        assertEquals(0, feed.get(1).likeCount());
        assertEquals(5, feed.get(1).commentCount());
        assertTrue(feed.get(1).likedByMe());
        assertEquals("bogdan", feed.get(1).author().username());
        assertEquals("/api/uploads/2-x.png", feed.get(1).author().avatarUrl());
    }

    @Test
    void feedFiltersBySportWhenGiven() {
        when(posts.findBySportIdOrderByCreatedAtDescIdDesc(eq(3L), any())).thenReturn(List.of());

        assertTrue(service.getFeed(ANA, 3L, 0, 20).isEmpty());

        verify(posts).findBySportIdOrderByCreatedAtDescIdDesc(eq(3L), any());
        verify(posts, never()).findAllByOrderByCreatedAtDescIdDesc(any());
    }

    @Test
    void feedAsksForTheRightPage() {
        when(posts.findAllByOrderByCreatedAtDescIdDesc(any())).thenReturn(List.of());

        service.getFeed(ANA, null, 2, 10);

        verify(posts).findAllByOrderByCreatedAtDescIdDesc(argThat((Pageable p) ->
                p.getPageNumber() == 2 && p.getPageSize() == 10));
    }

    @Test
    void feedRejectsInvalidPaging() {
        assertThrows(ApiException.class, () -> service.getFeed(ANA, null, -1, 20));
        assertThrows(ApiException.class, () -> service.getFeed(ANA, null, 0, 0));
        assertThrows(ApiException.class, () -> service.getFeed(ANA, null, 0, FeedService.MAX_PAGE_SIZE + 1));
    }

    // ---------- postare nouă ----------

    @Test
    void createPostTrimsTextAndStartsWithZeroCounts() {
        PostDto dto = service.createPost(ANA, "  Căutăm doi jucători.  ", 1L, PostTag.LOOKING_FOR_PLAYERS);

        assertEquals("Căutăm doi jucători.", dto.content());
        assertEquals(PostTag.LOOKING_FOR_PLAYERS, dto.tag());
        assertEquals(0, dto.likeCount());
        assertEquals(0, dto.commentCount());
        assertEquals("ana", dto.author().username());
    }

    @Test
    void createPostRejectsUnknownSport() {
        when(catalog.findSport(99L)).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class,
                () -> service.createPost(ANA, "Salut", 99L, PostTag.DISCUSSION));

        assertTrue(e.getFields().containsKey("sportId"));
        verify(posts, never()).save(any());
    }

    @Test
    void createPostRejectsBlankText() {
        assertThrows(ApiException.class, () -> service.createPost(ANA, "   ", 1L, PostTag.DISCUSSION));
        verify(posts, never()).save(any());
    }

    // ---------- like ----------

    @Test
    void firstLikeAddsIt() {
        when(posts.existsById(10L)).thenReturn(true);
        when(likes.findByPostIdAndUserId(10L, ANA)).thenReturn(Optional.empty());
        when(likes.countByPostId(10L)).thenReturn(1L);

        LikeResult r = service.toggleLike(ANA, 10L);

        assertTrue(r.liked());
        assertEquals(1, r.likeCount());
        verify(likes).save(argThat(l -> l.getPostId().equals(10L) && l.getUserId().equals(ANA)));
    }

    @Test
    void secondLikeRemovesIt() {
        PostLike existing = new PostLike(10L, ANA);
        when(posts.existsById(10L)).thenReturn(true);
        when(likes.findByPostIdAndUserId(10L, ANA)).thenReturn(Optional.of(existing));
        when(likes.countByPostId(10L)).thenReturn(0L);

        LikeResult r = service.toggleLike(ANA, 10L);

        assertFalse(r.liked());
        assertEquals(0, r.likeCount());
        verify(likes).delete(existing);
        verify(likes, never()).save(any());
    }

    @Test
    void likeOnMissingPostIsNotFound() {
        when(posts.existsById(404L)).thenReturn(false);
        ApiException e = assertThrows(ApiException.class, () -> service.toggleLike(ANA, 404L));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    // ---------- comentarii ----------

    @Test
    void addCommentReturnsItWithAuthor() {
        when(posts.existsById(10L)).thenReturn(true);

        CommentDto c = service.addComment(BOGDAN, 10L, " Vin și eu. ");

        assertEquals("Vin și eu.", c.content());
        assertEquals("bogdan", c.author().username());
    }

    @Test
    void commentOnMissingPostIsNotFound() {
        when(posts.existsById(404L)).thenReturn(false);
        assertThrows(ApiException.class, () -> service.addComment(ANA, 404L, "Salut"));
        verify(comments, never()).save(any());
    }

    @Test
    void deletedAuthorStillShowsAsPlaceholder() {
        when(posts.findAllByOrderByCreatedAtDescIdDesc(any())).thenReturn(List.of(post(1L, 77L)));
        when(likes.countByPostIds(any())).thenReturn(List.of());
        when(comments.countByPostIds(any())).thenReturn(List.of());
        when(likes.findLikedPostIds(any(), any())).thenReturn(List.of());

        PostDto p = service.getFeed(ANA, null, 0, 20).getFirst();

        assertEquals("Utilizator necunoscut", p.author().fullName());
    }

    @Test
    void feedShowsAuthorRatingAndAsksOncePerAuthor() {
        when(posts.findAllByOrderByCreatedAtDescIdDesc(any()))
                .thenReturn(List.of(post(1L, BOGDAN), post(2L, BOGDAN), post(3L, ANA)));
        when(likes.countByPostIds(any())).thenReturn(List.of());
        when(comments.countByPostIds(any())).thenReturn(List.of());
        when(likes.findLikedPostIds(any(), any())).thenReturn(List.of());
        when(ratings.getStats(BOGDAN)).thenReturn(new RatingStatsDto(BOGDAN, 4.2, 9, 90, List.of()));

        List<PostDto> feed = service.getFeed(ANA, null, 0, 20);

        assertEquals(4.2, feed.get(0).authorRating());
        assertEquals(4.2, feed.get(1).authorRating());
        assertNull(feed.get(2).authorRating());
        verify(ratings, times(1)).getStats(BOGDAN);
    }
}
