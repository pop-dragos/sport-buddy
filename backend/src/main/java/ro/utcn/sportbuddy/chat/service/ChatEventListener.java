package ro.utcn.sportbuddy.chat.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import ro.utcn.sportbuddy.common.events.LobbyCancelledEvent;
import ro.utcn.sportbuddy.common.events.LobbyLockedEvent;
import ro.utcn.sportbuddy.common.events.PlayerCancelledEvent;
import ro.utcn.sportbuddy.common.events.PlayerJoinedEvent;
import ro.utcn.sportbuddy.users.service.UserService;

// Mesajele de sistem din chat, ca reacție la ce se întâmplă în lobbies (B).
// Rulează doar după ce acțiunea lui B s-a salvat, fiecare în tranzacția ei.
@Component
public class ChatEventListener {

    private final ChatService chatService;
    private final UserService userService;

    public ChatEventListener(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPlayerJoined(PlayerJoinedEvent event) {
        chatService.addSystemMessage(event.lobbyId(), username(event.userId()) + " s-a înscris.");
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLobbyLocked(LobbyLockedEvent event) {
        String text = event.finalPrice() == null
                ? "Lobby confirmat."
                : "Lobby confirmat. Preț final: " + event.finalPrice() + " RON / persoană.";
        chatService.addSystemMessage(event.lobbyId(), text);
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPlayerCancelled(PlayerCancelledEvent event) {
        chatService.addSystemMessage(event.lobbyId(), username(event.userId()) + " a ieșit din lobby.");
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLobbyCancelled(LobbyCancelledEvent event) {
        chatService.addSystemMessage(event.lobbyId(), "Lobby-ul a fost anulat.");
    }

    private String username(Long userId) {
        return userService.getSummary(userId).username();
    }
}
