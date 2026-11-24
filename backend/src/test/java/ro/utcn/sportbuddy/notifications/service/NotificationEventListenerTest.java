package ro.utcn.sportbuddy.notifications.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.utcn.sportbuddy.common.events.FriendAcceptedEvent;
import ro.utcn.sportbuddy.common.events.FriendRequestedEvent;
import ro.utcn.sportbuddy.common.events.JoinRequestDecidedEvent;
import ro.utcn.sportbuddy.common.events.JoinRequestedEvent;
import ro.utcn.sportbuddy.common.events.LobbyCancelledEvent;
import ro.utcn.sportbuddy.common.events.LobbyLockedEvent;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.notifications.model.NotificationType;

import java.util.List;

import static org.mockito.Mockito.*;

class NotificationEventListenerTest {

    private static final Long LOBBY = 55L;
    private static final Long HOST = 1L, ANDREI = 2L, IOANA = 3L;

    private NotificationService notifications;
    private LobbyService lobbies;
    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        notifications = mock(NotificationService.class);
        lobbies = mock(LobbyService.class);
        listener = new NotificationEventListener(notifications, lobbies);
    }

    @Test
    void joinRequestGoesToTheHostFromTheRequester() {
        listener.onJoinRequested(new JoinRequestedEvent(LOBBY, ANDREI, HOST));

        verify(notifications).notify(HOST, NotificationType.JOIN_REQUESTED, LOBBY, ANDREI);
    }

    @Test
    void decisionGoesToTheRequester() {
        listener.onJoinRequestDecided(new JoinRequestDecidedEvent(LOBBY, ANDREI, true));
        listener.onJoinRequestDecided(new JoinRequestDecidedEvent(LOBBY, IOANA, false));

        verify(notifications).notify(ANDREI, NotificationType.JOIN_ACCEPTED, LOBBY, null);
        verify(notifications).notify(IOANA, NotificationType.JOIN_REJECTED, LOBBY, null);
    }

    @Test
    void lockedLobbyNotifiesEveryParticipant() {
        listener.onLobbyLocked(new LobbyLockedEvent(LOBBY, 20, List.of(HOST, ANDREI, IOANA)));

        verify(notifications).notify(HOST, NotificationType.LOBBY_LOCKED, LOBBY, null);
        verify(notifications).notify(ANDREI, NotificationType.LOBBY_LOCKED, LOBBY, null);
        verify(notifications).notify(IOANA, NotificationType.LOBBY_LOCKED, LOBBY, null);
    }

    @Test
    void cancelledLobbyNotifiesParticipantsWithTheHostAsActor() {
        when(lobbies.participantIds(LOBBY)).thenReturn(List.of(HOST, ANDREI));

        listener.onLobbyCancelled(new LobbyCancelledEvent(LOBBY, HOST, false));

        // pentru organizator, actorul e chiar el, deci NotificationService nu îi creează nimic
        verify(notifications).notify(HOST, NotificationType.LOBBY_CANCELLED, LOBBY, HOST);
        verify(notifications).notify(ANDREI, NotificationType.LOBBY_CANCELLED, LOBBY, HOST);
    }

    @Test
    void friendEventsGoToTheRightPerson() {
        listener.onFriendRequested(new FriendRequestedEvent(ANDREI, HOST));
        listener.onFriendAccepted(new FriendAcceptedEvent(ANDREI, HOST));

        verify(notifications).notify(HOST, NotificationType.FRIEND_REQUEST, null, ANDREI);
        verify(notifications).notify(ANDREI, NotificationType.FRIEND_ACCEPTED, null, HOST);
    }
}
