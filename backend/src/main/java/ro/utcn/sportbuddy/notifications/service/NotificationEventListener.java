package ro.utcn.sportbuddy.notifications.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import ro.utcn.sportbuddy.common.events.FriendAcceptedEvent;
import ro.utcn.sportbuddy.common.events.FriendRequestedEvent;
import ro.utcn.sportbuddy.common.events.JoinRequestDecidedEvent;
import ro.utcn.sportbuddy.common.events.JoinRequestedEvent;
import ro.utcn.sportbuddy.common.events.LobbyCancelledEvent;
import ro.utcn.sportbuddy.common.events.LobbyLockedEvent;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.notifications.model.NotificationType;

// Creează notificările ca reacție la ce se întâmplă în lobbies și friends.
// Rulează doar după ce acțiunea s-a salvat, fiecare în tranzacția ei: o notificare eșuată nu strică acțiunea.
@Component
public class NotificationEventListener {

    private final NotificationService notifications;
    private final LobbyService lobbyService;

    public NotificationEventListener(NotificationService notifications, LobbyService lobbyService) {
        this.notifications = notifications;
        this.lobbyService = lobbyService;
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onJoinRequested(JoinRequestedEvent e) {
        notifications.notify(e.hostUserId(), NotificationType.JOIN_REQUESTED, e.lobbyId(), e.requesterId());
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onJoinRequestDecided(JoinRequestDecidedEvent e) {
        notifications.notify(e.userId(), e.accepted() ? NotificationType.JOIN_ACCEPTED : NotificationType.JOIN_REJECTED,
                e.lobbyId(), null);
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLobbyLocked(LobbyLockedEvent e) {
        for (Long userId : e.participantUserIds()) {
            notifications.notify(userId, NotificationType.LOBBY_LOCKED, e.lobbyId(), null);
        }
    }

    // Participanții află că lobby-ul s-a anulat; organizatorul, care l-a anulat, nu primește nimic (actor = el).
    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLobbyCancelled(LobbyCancelledEvent e) {
        for (Long userId : lobbyService.participantIds(e.lobbyId())) {
            notifications.notify(userId, NotificationType.LOBBY_CANCELLED, e.lobbyId(), e.hostUserId());
        }
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onFriendRequested(FriendRequestedEvent e) {
        notifications.notify(e.addresseeId(), NotificationType.FRIEND_REQUEST, null, e.requesterId());
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onFriendAccepted(FriendAcceptedEvent e) {
        notifications.notify(e.requesterId(), NotificationType.FRIEND_ACCEPTED, null, e.accepterId());
    }
}
