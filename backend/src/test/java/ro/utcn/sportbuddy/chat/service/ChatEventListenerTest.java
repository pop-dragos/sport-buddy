package ro.utcn.sportbuddy.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.utcn.sportbuddy.common.events.LobbyCancelledEvent;
import ro.utcn.sportbuddy.common.events.LobbyLockedEvent;
import ro.utcn.sportbuddy.common.events.PlayerCancelledEvent;
import ro.utcn.sportbuddy.common.events.PlayerJoinedEvent;
import ro.utcn.sportbuddy.users.dto.UserSummary;
import ro.utcn.sportbuddy.users.service.UserService;

import java.util.List;

import static org.mockito.Mockito.*;

class ChatEventListenerTest {

    private static final Long LOBBY = 55L;
    private static final Long RADU = 21L;

    private ChatService chatService;
    private ChatEventListener listener;

    @BeforeEach
    void setUp() {
        chatService = mock(ChatService.class);
        UserService userService = mock(UserService.class);
        listener = new ChatEventListener(chatService, userService);
        when(userService.getSummary(RADU)).thenReturn(new UserSummary(RADU, "radu", "Radu Moldovan", "radu@demo.ro"));
    }

    @Test
    void joinedPlayerIsAnnounced() {
        listener.onPlayerJoined(new PlayerJoinedEvent(LOBBY, RADU));

        verify(chatService).addSystemMessage(LOBBY, "radu s-a înscris.");
    }

    @Test
    void lockShowsFinalPrice() {
        listener.onLobbyLocked(new LobbyLockedEvent(LOBBY, 34, List.of(7L, RADU)));

        verify(chatService).addSystemMessage(LOBBY, "Lobby confirmat. Preț final: 34 RON / persoană.");
    }

    @Test
    void lockOfFreeLobbyHasNoPrice() {
        listener.onLobbyLocked(new LobbyLockedEvent(LOBBY, null, List.of(7L)));

        verify(chatService).addSystemMessage(LOBBY, "Lobby confirmat.");
    }

    @Test
    void leavingPlayerIsAnnounced() {
        listener.onPlayerCancelled(new PlayerCancelledEvent(LOBBY, RADU, true));

        verify(chatService).addSystemMessage(LOBBY, "radu a ieșit din lobby.");
    }

    @Test
    void cancelledLobbyIsAnnounced() {
        listener.onLobbyCancelled(new LobbyCancelledEvent(LOBBY, 7L, false));

        verify(chatService).addSystemMessage(LOBBY, "Lobby-ul a fost anulat.");
    }
}
