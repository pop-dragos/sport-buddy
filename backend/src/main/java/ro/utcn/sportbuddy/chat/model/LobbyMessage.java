package ro.utcn.sportbuddy.chat.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "lobby_messages", indexes = @Index(columnList = "lobby_id"))
public class LobbyMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long lobbyId;

    // null pentru mesajele de sistem („X s-a înscris", „Lobby confirmat")
    private Long senderId;

    @Column(nullable = false)
    private boolean system;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false, updatable = false)
    private Instant sentAt;

    protected LobbyMessage() {}

    public LobbyMessage(Long lobbyId, Long senderId, boolean system, String text) {
        this.lobbyId = lobbyId;
        this.senderId = senderId;
        this.system = system;
        this.text = text;
        this.sentAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getLobbyId() { return lobbyId; }
    public Long getSenderId() { return senderId; }
    public boolean isSystem() { return system; }
    public String getText() { return text; }
    public Instant getSentAt() { return sentAt; }
}
