package ro.utcn.sportbuddy.friends.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import ro.utcn.sportbuddy.catalog.dto.SportDto;
import ro.utcn.sportbuddy.catalog.service.CatalogService;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.friends.dto.FriendItem;
import ro.utcn.sportbuddy.friends.dto.FriendStatus;
import ro.utcn.sportbuddy.friends.model.DirectMessage;
import ro.utcn.sportbuddy.friends.model.Friendship;
import ro.utcn.sportbuddy.friends.model.FriendshipStatus;
import ro.utcn.sportbuddy.friends.repository.DirectMessageRepository;
import ro.utcn.sportbuddy.friends.repository.FriendshipRepository;
import ro.utcn.sportbuddy.lobbies.dto.LobbyStats;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.ratings.dto.RatingStatsDto;
import ro.utcn.sportbuddy.ratings.service.RatingService;
import ro.utcn.sportbuddy.users.dto.PublicProfile;
import ro.utcn.sportbuddy.users.dto.SportLevelDto;
import ro.utcn.sportbuddy.users.dto.UserSettingsDto;
import ro.utcn.sportbuddy.users.model.ProfileVisibility;
import ro.utcn.sportbuddy.users.model.Theme;
import ro.utcn.sportbuddy.users.model.SportLevel;
import ro.utcn.sportbuddy.users.model.WhoCanAddMe;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class FriendServiceTest {

    private static final Long ANA = 1L, BOGDAN = 2L, IOANA = 3L, VLAD = 4L;

    private FriendshipRepository friendships;
    private DirectMessageRepository messages;
    private UserService users;
    private CatalogService catalog;
    private RatingService ratings;
    private LobbyService lobbies;
    private FriendService service;

    @BeforeEach
    void setUp() {
        friendships = mock(FriendshipRepository.class);
        messages = mock(DirectMessageRepository.class);
        users = mock(UserService.class);
        catalog = mock(CatalogService.class);
        ratings = mock(RatingService.class);
        lobbies = mock(LobbyService.class);
        service = new FriendService(friendships, messages, users, catalog, ratings, lobbies);

        when(users.isActive(anyLong())).thenReturn(true);
        when(users.whoCanAddMe(anyLong())).thenReturn(WhoCanAddMe.EVERYONE);
        when(friendships.findBetween(anyLong(), anyLong())).thenReturn(List.of());
        when(friendships.findAccepted(anyLong())).thenReturn(List.of());
        when(friendships.save(any())).thenAnswer(i -> withId(i.getArgument(0), 100L));
        when(catalog.getSports()).thenReturn(List.of(new SportDto(1L, "football", "Fotbal", 10, "Teren", false)));
        when(ratings.getStats(anyLong())).thenAnswer(i -> RatingStatsDto.newUser(i.getArgument(0)));
        when(lobbies.coPlayerIds(anyLong())).thenReturn(Set.of());
        when(lobbies.stats(anyLong(), anyLong())).thenReturn(new LobbyStats(0, 0));
    }

    private static Friendship withId(Friendship f, Long id) {
        ReflectionTestUtils.setField(f, "id", id);
        ReflectionTestUtils.setField(f, "createdAt", Instant.now());
        return f;
    }

    private static Friendship pending(Long id, Long from, Long to) {
        return withId(new Friendship(from, to), id);
    }

    private static Friendship accepted(Long id, Long a, Long b) {
        Friendship f = pending(id, a, b);
        f.accept();
        return f;
    }

    private static PublicProfile profile(Long id, String username, String fullName) {
        return new PublicProfile(id, username, fullName, null, null, null, LocalDate.now(),
                List.of(new SportLevelDto(1L, SportLevel.BEGINNER)), false);
    }

    private static ApiException error(Runnable action) {
        return assertThrows(ApiException.class, action::run);
    }

    // ---------- trimitere cerere ----------

    @Test
    void newRequestIsPending() {
        FriendStatus s = service.sendRequest(ANA, BOGDAN);

        assertEquals(FriendStatus.Status.PENDING_SENT, s.status());
        assertEquals(100L, s.requestId());
        verify(friendships).save(argThat(f -> f.getRequesterId().equals(ANA) && f.getAddresseeId().equals(BOGDAN)));
    }

    @Test
    void cannotAddYourself() {
        assertEquals(HttpStatus.BAD_REQUEST, error(() -> service.sendRequest(ANA, ANA)).getStatus());
    }

    @Test
    void cannotAddDeletedUser() {
        when(users.isActive(BOGDAN)).thenReturn(false);
        assertEquals(HttpStatus.NOT_FOUND, error(() -> service.sendRequest(ANA, BOGDAN)).getStatus());
    }

    @Test
    void alreadyFriendsIsConflict() {
        when(friendships.findBetween(ANA, BOGDAN)).thenReturn(List.of(accepted(5L, BOGDAN, ANA)));
        assertEquals("ALREADY_FRIENDS", error(() -> service.sendRequest(ANA, BOGDAN)).getCode());
    }

    @Test
    void sendingTwiceIsConflict() {
        when(friendships.findBetween(ANA, BOGDAN)).thenReturn(List.of(pending(5L, ANA, BOGDAN)));
        assertEquals("REQUEST_EXISTS", error(() -> service.sendRequest(ANA, BOGDAN)).getCode());
    }

    @Test
    void requestingBackAcceptsTheirRequest() {
        Friendship theirs = pending(5L, BOGDAN, ANA);
        when(friendships.findBetween(ANA, BOGDAN)).thenReturn(List.of(theirs));

        FriendStatus s = service.sendRequest(ANA, BOGDAN);

        assertEquals(FriendStatus.Status.FRIENDS, s.status());
        assertTrue(theirs.isAccepted());
        verify(friendships, never()).save(any());
    }

    @Test
    void declinedRequestCanBeSentAgain() {
        Friendship old = pending(5L, ANA, BOGDAN);
        old.decline();
        when(friendships.findBetween(ANA, BOGDAN)).thenReturn(List.of(old));

        assertEquals(FriendStatus.Status.PENDING_SENT, service.sendRequest(ANA, BOGDAN).status());

        verify(friendships).delete(old);
        verify(friendships).flush();
        verify(friendships).save(any());
    }

    @Test
    void nobodyCanAddWhenDisabled() {
        when(users.whoCanAddMe(BOGDAN)).thenReturn(WhoCanAddMe.NOBODY);
        ApiException e = error(() -> service.sendRequest(ANA, BOGDAN));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals("FRIEND_REQUESTS_DISABLED", e.getCode());
        verify(friendships, never()).save(any());
    }

    @Test
    void friendsOfFriendsNeedsACommonFriend() {
        when(users.whoCanAddMe(BOGDAN)).thenReturn(WhoCanAddMe.FRIENDS_OF_FRIENDS);
        assertEquals("FRIEND_REQUESTS_DISABLED", error(() -> service.sendRequest(ANA, BOGDAN)).getCode());

        when(friendships.findAccepted(ANA)).thenReturn(List.of(accepted(7L, ANA, IOANA)));
        when(friendships.findAccepted(BOGDAN)).thenReturn(List.of(accepted(8L, IOANA, BOGDAN)));
        assertEquals(FriendStatus.Status.PENDING_SENT, service.sendRequest(ANA, BOGDAN).status());
    }

    // ---------- răspuns la cerere ----------

    @Test
    void addresseeCanAccept() {
        Friendship f = pending(5L, BOGDAN, ANA);
        when(friendships.findById(5L)).thenReturn(Optional.of(f));

        service.accept(ANA, 5L);

        assertTrue(f.isAccepted());
        assertNotNull(f.getRespondedAt());
    }

    @Test
    void addresseeCanReject() {
        Friendship f = pending(5L, BOGDAN, ANA);
        when(friendships.findById(5L)).thenReturn(Optional.of(f));

        service.reject(ANA, 5L);

        assertEquals(FriendshipStatus.DECLINED, f.getStatus());
    }

    @Test
    void senderCannotAcceptOwnRequest() {
        when(friendships.findById(5L)).thenReturn(Optional.of(pending(5L, ANA, BOGDAN)));
        assertEquals(HttpStatus.NOT_FOUND, error(() -> service.accept(ANA, 5L)).getStatus());
    }

    @Test
    void answeredRequestCannotBeAnsweredAgain() {
        when(friendships.findById(5L)).thenReturn(Optional.of(accepted(5L, BOGDAN, ANA)));
        assertEquals("REQUEST_NOT_PENDING", error(() -> service.reject(ANA, 5L)).getCode());
    }

    @Test
    void onlySenderCanCancel() {
        Friendship f = pending(5L, ANA, BOGDAN);
        when(friendships.findById(5L)).thenReturn(Optional.of(f));

        assertEquals(HttpStatus.NOT_FOUND, error(() -> service.cancel(BOGDAN, 5L)).getStatus());
        service.cancel(ANA, 5L);

        verify(friendships).delete(f);
    }

    @Test
    void removeFriendDeletesTheFriendship() {
        Friendship f = accepted(5L, ANA, BOGDAN);
        when(friendships.findBetween(ANA, BOGDAN)).thenReturn(List.of(f));

        service.removeFriend(ANA, BOGDAN);

        verify(friendships).delete(f);
    }

    // ---------- stare ----------

    @Test
    void statusFromBothSides() {
        assertEquals(FriendStatus.Status.NONE, service.status(ANA, BOGDAN).status());

        when(friendships.findBetween(ANA, BOGDAN)).thenReturn(List.of(pending(5L, ANA, BOGDAN)));
        assertEquals(new FriendStatus(FriendStatus.Status.PENDING_SENT, 5L), service.status(ANA, BOGDAN));

        when(friendships.findBetween(BOGDAN, ANA)).thenReturn(List.of(pending(5L, ANA, BOGDAN)));
        assertEquals(new FriendStatus(FriendStatus.Status.PENDING_RECEIVED, 5L), service.status(BOGDAN, ANA));

        when(friendships.findBetween(ANA, BOGDAN)).thenReturn(List.of(accepted(5L, ANA, BOGDAN)));
        assertEquals(FriendStatus.Status.FRIENDS, service.status(ANA, BOGDAN).status());
        assertTrue(service.areFriends(ANA, BOGDAN));
    }

    // ---------- liste ----------

    @Test
    void friendListSearchIgnoresDiacriticsAndCase() {
        when(friendships.findAccepted(ANA)).thenReturn(List.of(accepted(5L, ANA, BOGDAN), accepted(6L, IOANA, ANA)));
        Map<Long, PublicProfile> profiles = new LinkedHashMap<>();
        profiles.put(BOGDAN, profile(BOGDAN, "stefan", "Ștefan Luca"));
        profiles.put(IOANA, profile(IOANA, "ioana", "Ioana Marin"));
        when(users.findProfiles(any())).thenReturn(profiles);

        assertEquals(2, service.listFriends(ANA, null).size());
        List<FriendItem> found = service.listFriends(ANA, "STEF");

        assertEquals(1, found.size());
        assertEquals("Ștefan Luca", found.getFirst().fullName());
        assertEquals(List.of("football"), found.getFirst().sportCodes());
        assertEquals(0, found.getFirst().matchesTogether());
    }

    @Test
    void suggestionsSkipFriendsAndPendingButNotDeclined() {
        Friendship declined = pending(9L, ANA, VLAD);
        declined.decline();
        when(friendships.findInvolving(ANA)).thenReturn(List.of(accepted(5L, ANA, BOGDAN),
                pending(6L, IOANA, ANA), declined));
        when(users.findSimilarUsers(ANA)).thenReturn(List.of(BOGDAN, IOANA, VLAD, 9L));
        when(users.findProfiles(List.of(VLAD, 9L))).thenReturn(Map.of(
                VLAD, profile(VLAD, "vlad", "Vlad Pop"), 9L, profile(9L, "diana", "Diana Avram")));

        List<FriendItem> s = service.suggestions(ANA);

        assertEquals(Set.of(VLAD, 9L), new HashSet<>(s.stream().map(FriendItem::userId).toList()));
    }

    // ---------- mesaje ----------

    @Test
    void messagesOnlyBetweenFriends() {
        assertEquals("NOT_FRIENDS", error(() -> service.messages(ANA, BOGDAN, null)).getCode());
        assertEquals("NOT_FRIENDS", error(() -> service.sendMessage(ANA, BOGDAN, "Salut")).getCode());
        verify(messages, never()).save(any());
    }

    @Test
    void sendMessageTrimsAndRejectsBlank() {
        when(friendships.findBetween(ANA, BOGDAN)).thenReturn(List.of(accepted(5L, ANA, BOGDAN)));
        when(messages.save(any())).thenAnswer(i -> {
            DirectMessage m = i.getArgument(0);
            ReflectionTestUtils.setField(m, "id", 1L);
            m.setSentAt(Instant.now());
            return m;
        });

        assertEquals("Salut", service.sendMessage(ANA, BOGDAN, "  Salut  ").message());
        assertThrows(ApiException.class, () -> service.sendMessage(ANA, BOGDAN, "   "));
    }

    @Test
    void readingMarksTheirMessagesAsRead() {
        when(friendships.findBetween(ANA, BOGDAN)).thenReturn(List.of(accepted(5L, ANA, BOGDAN)));
        when(messages.findAfter(ANA, BOGDAN, 10L)).thenReturn(List.of());

        service.messages(ANA, BOGDAN, 10L);

        verify(messages).markRead(eq(BOGDAN), eq(ANA), any());
        verify(messages, never()).findLatest(anyLong(), anyLong(), any());
    }

    // ---------- legătura cu lobby-urile ----------

    private Map<Long, PublicProfile> profilesFor(Long... ids) {
        Map<Long, PublicProfile> m = new LinkedHashMap<>();
        for (Long id : ids) m.put(id, profile(id, "u" + id, "Utilizator " + id));
        return m;
    }

    @Test
    void suggestionsStartWithCoPlayersMostMatchesFirstThenSimilar() {
        when(friendships.findInvolving(ANA)).thenReturn(List.of(accepted(5L, ANA, BOGDAN)));
        // Bogdan e deja prieten, deci nu apare deși a jucat cu Ana
        when(lobbies.coPlayerIds(ANA)).thenReturn(Set.of(BOGDAN, IOANA, VLAD));
        when(lobbies.stats(IOANA, ANA)).thenReturn(new LobbyStats(4, 1));
        when(lobbies.stats(VLAD, ANA)).thenReturn(new LobbyStats(6, 3));
        when(users.findSimilarUsers(ANA)).thenReturn(List.of(VLAD, 9L, BOGDAN));
        when(users.findProfiles(any())).thenAnswer(i -> {
            Collection<Long> ids = i.getArgument(0);
            return profilesFor(ids.toArray(Long[]::new));
        });

        List<Long> order = service.suggestions(ANA).stream().map(FriendItem::userId).toList();

        assertEquals(List.of(VLAD, IOANA, 9L), order);
    }

    @Test
    void suggestionsAreCappedAtTheMaximum() {
        Set<Long> many = new HashSet<>();
        for (long id = 100; id < 100 + FriendService.MAX_SUGGESTIONS + 5; id++) many.add(id);
        when(friendships.findInvolving(ANA)).thenReturn(List.of());
        when(lobbies.coPlayerIds(ANA)).thenReturn(many);
        when(users.findProfiles(any())).thenAnswer(i -> {
            Collection<Long> ids = i.getArgument(0);
            return profilesFor(ids.toArray(Long[]::new));
        });

        assertEquals(FriendService.MAX_SUGGESTIONS, service.suggestions(ANA).size());
        verify(users, never()).findSimilarUsers(anyLong());
    }

    @Test
    void friendListShowsMatchesTogether() {
        when(friendships.findAccepted(ANA)).thenReturn(List.of(accepted(5L, ANA, BOGDAN)));
        when(users.findProfiles(any())).thenReturn(profilesFor(BOGDAN));
        when(lobbies.stats(BOGDAN, ANA)).thenReturn(new LobbyStats(10, 3));

        assertEquals(3, service.listFriends(ANA, null).getFirst().matchesTogether());
    }

    // ---------- prietenii altcuiva ----------

    private void bogdanSettings(boolean showFriendsList, ProfileVisibility visibility) {
        when(users.getSettings(BOGDAN)).thenReturn(new UserSettingsDto(Theme.SYSTEM, true, true, true, true, false,
                visibility, WhoCanAddMe.EVERYONE, showFriendsList));
        when(friendships.findAccepted(BOGDAN)).thenReturn(List.of(accepted(8L, BOGDAN, IOANA)));
        when(users.findProfiles(any())).thenReturn(profilesFor(IOANA));
    }

    @Test
    void friendsOfSomeoneAreVisibleWhenAllowed() {
        bogdanSettings(true, ProfileVisibility.EVERYONE);

        List<FriendItem> list = service.friendsOf(ANA, BOGDAN);

        assertEquals(List.of(IOANA), list.stream().map(FriendItem::userId).toList());
    }

    @Test
    void friendsListHiddenBySetting() {
        bogdanSettings(false, ProfileVisibility.EVERYONE);
        ApiException e = error(() -> service.friendsOf(ANA, BOGDAN));
        assertEquals(HttpStatus.FORBIDDEN, e.getStatus());
        assertEquals("FRIEND_LIST_HIDDEN", e.getCode());
    }

    @Test
    void friendsOnlyProfileHidesListFromStrangersButNotFriends() {
        bogdanSettings(true, ProfileVisibility.FRIENDS);
        assertEquals("FRIEND_LIST_HIDDEN", error(() -> service.friendsOf(ANA, BOGDAN)).getCode());

        when(friendships.findBetween(ANA, BOGDAN)).thenReturn(List.of(accepted(5L, ANA, BOGDAN)));
        assertEquals(1, service.friendsOf(ANA, BOGDAN).size());
    }

    @Test
    void ownFriendListIsAlwaysVisible() {
        bogdanSettings(false, ProfileVisibility.FRIENDS);
        assertEquals(1, service.friendsOf(BOGDAN, BOGDAN).size());
        verify(users, never()).getSettings(BOGDAN);
    }

    @Test
    void friendsOfDeletedUserIsNotFound() {
        when(users.isActive(BOGDAN)).thenReturn(false);
        assertEquals(HttpStatus.NOT_FOUND, error(() -> service.friendsOf(ANA, BOGDAN)).getStatus());
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
