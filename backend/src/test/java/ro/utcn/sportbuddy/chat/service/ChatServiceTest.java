package ro.utcn.sportbuddy.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import ro.utcn.sportbuddy.chat.dto.ChatMessageDto;
import ro.utcn.sportbuddy.chat.model.LobbyMessage;
import ro.utcn.sportbuddy.chat.repository.LobbyMessageRepository;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.lobbies.dto.LobbyDetails;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.users.dto.UserSummary;
import ro.utcn.sportbuddy.users.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

// Repository-ul și service-urile celorlalte module sunt simulate, deci testul nu are nevoie de bază de date.
class ChatServiceTest {

    private static final Long LOBBY = 55L;
    private static final Long ANA = 1L;
    private static final Long ANDREI = 2L;

    private LobbyMessageRepository messageRepository;
    private LobbyService lobbyService;
    private UserService userService;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        messageRepository = mock(LobbyMessageRepository.class);
        lobbyService = mock(LobbyService.class);
        userService = mock(UserService.class);
        chatService = new ChatService(messageRepository, lobbyService, userService);

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
                new LobbyMessage(LOBBY, ANDREI, false, "al doilea"),
                new LobbyMessage(LOBBY, ANA, false, "primul")));

        List<ChatMessageDto> messages = chatService.getMessages(LOBBY, ANA, null);

        assertEquals(List.of("primul", "al doilea"), messages.stream().map(ChatMessageDto::message).toList());
    }

    @Test
    void afterReturnsOnlyNewerMessages() {
        when(lobbyService.isParticipant(LOBBY, ANA)).thenReturn(true);
        when(messageRepository.findByLobbyIdAndIdGreaterThanOrderByIdAsc(LOBBY, 120L))
                .thenReturn(List.of(new LobbyMessage(LOBBY, ANDREI, false, "nou")));

        List<ChatMessageDto> messages = chatService.getMessages(LOBBY, ANA, 120L);

        assertEquals(1, messages.size());
        verify(messageRepository, never()).findTop100ByLobbyIdOrderByIdDesc(any());
    }

    @Test
    void systemMessageHasNoSender() {
        when(lobbyService.isParticipant(LOBBY, ANA)).thenReturn(true);
        when(messageRepository.findTop100ByLobbyIdOrderByIdDesc(LOBBY))
                .thenReturn(List.of(new LobbyMessage(LOBBY, null, true, "Lobby confirmat")));

        ChatMessageDto dto = chatService.getMessages(LOBBY, ANA, null).get(0);

        assertEquals("SYSTEM", dto.type());
        assertNull(dto.senderId());
        assertNull(dto.senderUsername());
        verifyNoInteractions(userService);
    }

    @Test
    void eachAuthorIsLookedUpOnce() {
        when(lobbyService.isParticipant(LOBBY, ANA)).thenReturn(true);
        when(messageRepository.findTop100ByLobbyIdOrderByIdDesc(LOBBY)).thenReturn(List.of(
                new LobbyMessage(LOBBY, ANDREI, false, "3"),
                new LobbyMessage(LOBBY, ANDREI, false, "2"),
                new LobbyMessage(LOBBY, ANDREI, false, "1")));

        chatService.getMessages(LOBBY, ANA, null);

        verify(userService, times(1)).getSummary(ANDREI);
    }

    // Lobby-ul există; conținutul nu contează pentru chat.
    private static LobbyDetails lobby() {
        return new LobbyDetails(LOBBY, null, null, null, null, "OPEN", null,
                10, 6, 0, 1, 9, null, 0, null, null, null, null,
                false, false, "NONE", false, List.of(), List.of());
    }
}
