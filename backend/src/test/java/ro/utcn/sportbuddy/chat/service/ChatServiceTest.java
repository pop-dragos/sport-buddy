package ro.utcn.sportbuddy.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import ro.utcn.sportbuddy.chat.dto.ChatMessageDto;
import ro.utcn.sportbuddy.chat.model.LobbyChatRead;
import ro.utcn.sportbuddy.chat.model.LobbyMessage;
import ro.utcn.sportbuddy.chat.repository.LobbyChatReadRepository;
import ro.utcn.sportbuddy.chat.repository.LobbyMessageRepository;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.lobbies.dto.LobbyDetails;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.users.dto.UserSummary;
import ro.utcn.sportbuddy.users.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// Repository-ul și service-urile celorlalte module sunt simulate, deci testul nu are nevoie de bază de date.
class ChatServiceTest {

    private static final Long LOBBY = 55L;
    private static final Long ANA = 1L;
    private static final Long ANDREI = 2L;

    private LobbyMessageRepository messageRepository;
    private LobbyChatReadRepository readRepository;
    private LobbyService lobbyService;
    private UserService userService;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        messageRepository = mock(LobbyMessageRepository.class);
        readRepository = mock(LobbyChatReadRepository.class);
        lobbyService = mock(LobbyService.class);
        userService = mock(UserService.class);
        chatService = new ChatService(messageRepository, readRepository, lobbyService, userService);

        when(userService.getSummary(ANA)).thenReturn(new UserSummary(ANA, "ana", "Ana Popescu", "ana@ex.ro"));
        when(userService.getSummary(ANDREI)).thenReturn(new UserSummary(ANDREI, "andrei", "Andrei Ionescu", "andrei@ex.ro"));
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void participantCanSendMessage() {
        when(lobbyService.isParticipant(LOBBY, ANDREI)).thenReturn(true);

        ChatMessageDto dto = chatService.send(LOBBY, ANDREI, "  Ne vedem la 18.  ");

        assertEquals("USER", dto.type());
        assertEquals(ANDREI, dto.senderId());
        assertEquals("andrei", dto.senderUsername());
        assertEquals("Ne vedem la 18.", dto.message()); // fără spațiile de la capete
        assertNotNull(dto.sentAt());
    }

    @Test
    void participantCheckDoesNotLoadWholeLobby() {
        when(lobbyService.isParticipant(LOBBY, ANA)).thenReturn(true);

        chatService.getMessages(LOBBY, ANA, 10L);

        // interogarea la 3 secunde rămâne ieftină
        verify(lobbyService, never()).getDetails(anyLong(), anyLong());
    }

    @Test
    void outsiderCannotSendOrRead() {
        when(lobbyService.isParticipant(LOBBY, ANDREI)).thenReturn(false);
        when(lobbyService.getDetails(LOBBY, ANDREI)).thenReturn(lobby());

        ApiException send = assertThrows(ApiException.class, () -> chatService.send(LOBBY, ANDREI, "Salut"));
        ApiException read = assertThrows(ApiException.class, () -> chatService.getMessages(LOBBY, ANDREI, null));

        assertEquals(HttpStatus.FORBIDDEN, send.getStatus());
        assertEquals("NOT_PARTICIPANT", send.getCode());
        assertEquals("NOT_PARTICIPANT", read.getCode());
        verify(messageRepository, never()).save(any());
    }

    @Test
    void missingLobbyIsNotFound() {
        when(lobbyService.isParticipant(999L, ANA)).thenReturn(false);
        when(lobbyService.getDetails(999L, ANA))
                .thenThrow(new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Lobby-ul nu există."));

        ApiException e = assertThrows(ApiException.class, () -> chatService.getMessages(999L, ANA, null));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    void historyIsReturnedOldestFirst() {
        when(lobbyService.isParticipant(LOBBY, ANA)).thenReturn(true);
        // repository-ul le dă de la cel mai nou la cel mai vechi
        when(messageRepository.findTop100ByLobbyIdOrderByIdDesc(LOBBY)).thenReturn(List.of(
                message(2L, ANDREI, "al doilea"),
                message(1L, ANA, "primul")));

        List<ChatMessageDto> messages = chatService.getMessages(LOBBY, ANA, null);

        assertEquals(List.of("primul", "al doilea"), messages.stream().map(ChatMessageDto::message).toList());
    }

    @Test
    void afterReturnsOnlyNewerMessages() {
        when(lobbyService.isParticipant(LOBBY, ANA)).thenReturn(true);
        when(messageRepository.findByLobbyIdAndIdGreaterThanOrderByIdAsc(LOBBY, 120L))
                .thenReturn(List.of(message(121L, ANDREI, "nou")));

        List<ChatMessageDto> messages = chatService.getMessages(LOBBY, ANA, 120L);

        assertEquals(1, messages.size());
        verify(messageRepository, never()).findTop100ByLobbyIdOrderByIdDesc(any());
    }

    @Test
    void systemMessageHasNoSender() {
        when(lobbyService.isParticipant(LOBBY, ANA)).thenReturn(true);
        when(messageRepository.findTop100ByLobbyIdOrderByIdDesc(LOBBY))
                .thenReturn(List.of(withId(7L, new LobbyMessage(LOBBY, null, true, "Lobby confirmat"))));

        ChatMessageDto dto = chatService.getMessages(LOBBY, ANA, null).get(0);

        assertEquals("SYSTEM", dto.type());
        assertNull(dto.senderId());
        assertNull(dto.senderUsername());
        verifyNoInteractions(userService);
    }

    @Test
    void systemMessageIsSavedWithoutSenderOrParticipantCheck() {
        chatService.addSystemMessage(LOBBY, "Lobby confirmat.");

        verify(messageRepository).save(argThat(m ->
                m.isSystem() && m.getSenderId() == null && m.getText().equals("Lobby confirmat.")));
        verifyNoInteractions(lobbyService);
    }

    @Test
    void eachAuthorIsLookedUpOnce() {
        when(lobbyService.isParticipant(LOBBY, ANA)).thenReturn(true);
        when(messageRepository.findTop100ByLobbyIdOrderByIdDesc(LOBBY)).thenReturn(List.of(
                message(3L, ANDREI, "3"),
                message(2L, ANDREI, "2"),
                message(1L, ANDREI, "1")));

        chatService.getMessages(LOBBY, ANA, null);

        verify(userService, times(1)).getSummary(ANDREI);
    }

    // ---------- mesaje necitite ----------

    @Test
    void readingMessagesMarksThemReadUpToTheLast() {
        when(lobbyService.isParticipant(LOBBY, ANA)).thenReturn(true);
        when(messageRepository.findTop100ByLobbyIdOrderByIdDesc(LOBBY))
                .thenReturn(List.of(message(12L, ANDREI, "b"), message(11L, ANDREI, "a")));

        chatService.getMessages(LOBBY, ANA, null);

        verify(readRepository).save(argThat(r ->
                r.getLobbyId().equals(LOBBY) && r.getUserId().equals(ANA) && r.getLastReadId() == 12L));
    }

    @Test
    void emptyPollDoesNotTouchTheReadMarker() {
        when(lobbyService.isParticipant(LOBBY, ANA)).thenReturn(true);
        when(messageRepository.findByLobbyIdAndIdGreaterThanOrderByIdAsc(LOBBY, 12L)).thenReturn(List.of());

        chatService.getMessages(LOBBY, ANA, 12L);

        verifyNoInteractions(readRepository);
    }

    @Test
    void readMarkerNeverMovesBackwards() {
        when(lobbyService.isParticipant(LOBBY, ANA)).thenReturn(true);
        when(readRepository.findByLobbyIdAndUserId(LOBBY, ANA)).thenReturn(Optional.of(new LobbyChatRead(LOBBY, ANA, 50L)));
        when(messageRepository.findByLobbyIdAndIdGreaterThanOrderByIdAsc(LOBBY, 5L))
                .thenReturn(List.of(message(20L, ANDREI, "vechi")));

        chatService.getMessages(LOBBY, ANA, 5L);

        verify(readRepository).save(argThat(r -> r.getLastReadId() == 50L));
    }

    @Test
    void unreadCountsOnlyNewerMessagesFromOthers() {
        when(lobbyService.participatingLobbyIds(ANA)).thenReturn(Set.of(LOBBY, 56L, 57L));
        when(readRepository.findByUserIdAndLobbyIdIn(eq(ANA), any()))
                .thenReturn(List.of(new LobbyChatRead(LOBBY, ANA, 100L), new LobbyChatRead(56L, ANA, 10L)));
        when(messageRepository.countByLobbyIdAndSystemFalseAndSenderIdNotAndIdGreaterThan(LOBBY, ANA, 100L)).thenReturn(3L);
        when(messageRepository.countByLobbyIdAndSystemFalseAndSenderIdNotAndIdGreaterThan(56L, ANA, 10L)).thenReturn(0L);
        // lobby-ul 57: chat-ul nu a fost deschis niciodată, deci se numără de la început
        when(messageRepository.countByLobbyIdAndSystemFalseAndSenderIdNotAndIdGreaterThan(57L, ANA, 0L)).thenReturn(2L);

        Map<Long, Long> unread = chatService.unreadByLobby(ANA);

        assertEquals(Map.of(LOBBY, 3L, 57L, 2L), unread);
    }

    @Test
    void unreadIsEmptyWithoutLobbies() {
        when(lobbyService.participatingLobbyIds(ANA)).thenReturn(Set.of());

        assertTrue(chatService.unreadByLobby(ANA).isEmpty());
        verifyNoInteractions(messageRepository);
    }

    // Mesajele din teste nu trec prin bază de date, deci id-ul li se pune de mână.
    private static LobbyMessage message(Long id, Long senderId, String text) {
        return withId(id, new LobbyMessage(LOBBY, senderId, false, text));
    }

    private static LobbyMessage withId(Long id, LobbyMessage m) {
        ReflectionTestUtils.setField(m, "id", id);
        return m;
    }

    // Lobby-ul există; conținutul nu contează pentru chat.
    private static LobbyDetails lobby() {
        return new LobbyDetails(LOBBY, null, null, null, null, "OPEN", null,
                10, 6, 0, 1, 9, null, 0, null, null, null, null,
                false, false, "NONE", false, List.of(), List.of());
    }
}
