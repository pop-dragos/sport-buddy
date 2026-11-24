package ro.utcn.sportbuddy.inbox.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ro.utcn.sportbuddy.chat.model.LobbyMessage;
import ro.utcn.sportbuddy.chat.service.ChatService;
import ro.utcn.sportbuddy.friends.model.DirectMessage;
import ro.utcn.sportbuddy.inbox.dto.ConversationDto;
import ro.utcn.sportbuddy.inbox.repository.InboxLobbyRepository;
import ro.utcn.sportbuddy.inbox.repository.InboxRepository;
import ro.utcn.sportbuddy.lobbies.dto.LobbySummary;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.users.dto.PublicProfile;
import ro.utcn.sportbuddy.users.service.FriendshipChecker;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class InboxServiceTest {

    private static final Long ME = 1L, BOGDAN = 2L, IOANA = 3L, EX_PRIETEN = 4L, LOBBY = 10L;

    private InboxRepository direct;
    private InboxLobbyRepository group;
    private UserService users;
    private FriendshipChecker friendships;
    private LobbyService lobbies;
    private ChatService chat;
    private InboxService service;

    @BeforeEach
    void setUp() {
        direct = mock(InboxRepository.class);
        group = mock(InboxLobbyRepository.class);
        users = mock(UserService.class);
        friendships = mock(FriendshipChecker.class);
        lobbies = mock(LobbyService.class);
        chat = mock(ChatService.class);
        service = new InboxService(direct, group, users, friendships, lobbies, chat);

        when(direct.findLastPerConversation(anyLong())).thenReturn(List.of());
        when(direct.countUnreadBySender(anyLong())).thenReturn(List.of());
        when(lobbies.participatingLobbyIds(anyLong())).thenReturn(Set.of());
        when(chat.unreadByLobby(anyLong())).thenReturn(Map.of());
        when(users.findProfiles(anyCollection())).thenReturn(Map.of());
    }

    private static PublicProfile profile(Long id, String username) {
        return new PublicProfile(id, username, username, null, null, null, null, List.of());
    }

    private static DirectMessage directMessage(Long from, Long to, String text, Instant at) {
        DirectMessage m = new DirectMessage(from, to, text);
        m.setSentAt(at);
        return m;
    }

    private static LobbyMessage lobbyMessage(Long sender, boolean system, String text, Instant at) {
        LobbyMessage m = new LobbyMessage(LOBBY, sender, system, text);
        ReflectionTestUtils.setField(m, "sentAt", at);
        return m;
    }

    private static LobbySummary lobbySummary() {
        return new LobbySummary(LOBBY, 1L, "football", "Fotbal", 5L, "Teren de fotbal", 1L, "Baia Mare",
                LocalDateTime.of(2026, 11, 25, 20, 0), "OPEN", 10, 8, 2, null, "JOINED", false, 0);
    }

    @Test
    void conversatiaDirectaArataUltimulMesajSiNecititele() {
        Instant now = Instant.now();
        when(direct.findLastPerConversation(ME)).thenReturn(List.of(
                directMessage(BOGDAN, ME, "Ne vedem la 8?", now),
                directMessage(ME, IOANA, "Multumesc!", now.minusSeconds(60))));
        when(users.findProfiles(anyCollection())).thenReturn(Map.of(
                BOGDAN, profile(BOGDAN, "bogdan"), IOANA, profile(IOANA, "ioana")));
        when(friendships.areFriends(anyLong(), anyLong())).thenReturn(true);
        when(direct.countUnreadBySender(ME)).thenReturn(List.<Object[]>of(new Object[]{BOGDAN, 3L}));

        List<ConversationDto> result = service.conversations(ME);

        assertEquals(2, result.size());
        assertEquals("DIRECT", result.get(0).type());
        assertEquals("bogdan", result.get(0).title());
        assertEquals("Ne vedem la 8?", result.get(0).lastMessage());
        assertFalse(result.get(0).lastFromMe());
        assertEquals(3, result.get(0).unread());
        assertTrue(result.get(1).lastFromMe());
        assertEquals(0, result.get(1).unread());
    }

    @Test
    void conversatiaCuUnUtilizatorCareNuMaiEstePrietenNuApare() {
        when(direct.findLastPerConversation(ME)).thenReturn(List.of(directMessage(EX_PRIETEN, ME, "Salut", Instant.now())));
        when(users.findProfiles(anyCollection())).thenReturn(Map.of(EX_PRIETEN, profile(EX_PRIETEN, "fost")));
        when(friendships.areFriends(ME, EX_PRIETEN)).thenReturn(false);

        assertTrue(service.conversations(ME).isEmpty());
    }

    @Test
    void contulStersNuApareInLista() {
        when(direct.findLastPerConversation(ME)).thenReturn(List.of(directMessage(BOGDAN, ME, "Salut", Instant.now())));

        assertTrue(service.conversations(ME).isEmpty());
    }

    @Test
    void grupulArataNumeleLobbyuluiSiCineAScrisUltimul() {
        when(lobbies.participatingLobbyIds(ME)).thenReturn(Set.of(LOBBY));
        when(group.findLastPerLobby(anyCollection())).thenReturn(List.of(
                lobbyMessage(BOGDAN, false, "Aduc eu mingea", Instant.now())));
        when(lobbies.summariesOf(anyCollection(), eq(ME))).thenReturn(List.of(lobbySummary()));
        when(users.findProfiles(anyCollection())).thenReturn(Map.of(BOGDAN, profile(BOGDAN, "bogdan")));
        when(chat.unreadByLobby(ME)).thenReturn(Map.of(LOBBY, 4L));

        List<ConversationDto> result = service.conversations(ME);

        assertEquals(1, result.size());
        ConversationDto c = result.get(0);
        assertEquals("GROUP", c.type());
        assertEquals(LOBBY, c.id());
        assertEquals("Fotbal", c.title());
        assertEquals("Teren de fotbal", c.place());
        assertEquals("bogdan", c.lastSender());
        assertEquals("Aduc eu mingea", c.lastMessage());
        assertEquals(4, c.unread());
        assertEquals("football", c.sportCode());
    }

    @Test
    void mesajulDeSistemDinGrupNuAreAutor() {
        when(lobbies.participatingLobbyIds(ME)).thenReturn(Set.of(LOBBY));
        when(group.findLastPerLobby(anyCollection())).thenReturn(List.of(
                lobbyMessage(null, true, "Lobby-ul a fost confirmat.", Instant.now())));
        when(lobbies.summariesOf(anyCollection(), eq(ME))).thenReturn(List.of(lobbySummary()));

        ConversationDto c = service.conversations(ME).get(0);

        assertNull(c.lastSender());
        assertFalse(c.lastFromMe());
    }

    @Test
    void listaMestecaDirecteleSiGrupurileDupaData() {
        Instant now = Instant.now();
        when(direct.findLastPerConversation(ME)).thenReturn(List.of(
                directMessage(BOGDAN, ME, "Vechi", now.minusSeconds(3600))));
        when(users.findProfiles(anyCollection())).thenReturn(Map.of(BOGDAN, profile(BOGDAN, "bogdan")));
        when(friendships.areFriends(anyLong(), anyLong())).thenReturn(true);
        when(lobbies.participatingLobbyIds(ME)).thenReturn(Set.of(LOBBY));
        when(group.findLastPerLobby(anyCollection())).thenReturn(List.of(lobbyMessage(BOGDAN, false, "Nou", now)));
        when(lobbies.summariesOf(anyCollection(), eq(ME))).thenReturn(List.of(lobbySummary()));

        List<ConversationDto> result = service.conversations(ME);

        assertEquals("GROUP", result.get(0).type());
        assertEquals("DIRECT", result.get(1).type());
    }

    @Test
    void totalulDeNecititeAdunaDirecteleSiGrupurile() {
        when(direct.countUnread(ME)).thenReturn(5L);
        when(chat.unreadByLobby(ME)).thenReturn(Map.of(LOBBY, 2L, 11L, 1L));

        assertEquals(8L, service.unreadCount(ME));
    }
}
