package ro.utcn.sportbuddy.chat.model;

import jakarta.persistence.*;

// Până la ce mesaj din chat-ul unui lobby a citit un utilizator. Mesajele cu id mai mare, scrise de alții, sunt necitite.
@Entity
@Table(name = "lobby_chat_reads",
        uniqueConstraints = @UniqueConstraint(columnNames = {"lobby_id", "user_id"}))
public class LobbyChatRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long lobbyId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long lastReadId;

    protected LobbyChatRead() {}

    public LobbyChatRead(Long lobbyId, Long userId, Long lastReadId) {
        this.lobbyId = lobbyId;
        this.userId = userId;
        this.lastReadId = lastReadId;
    }

    // doar înainte: un răspuns întârziat nu mută citirea înapoi
    public void advanceTo(Long messageId) {
        if (messageId > lastReadId) lastReadId = messageId;
    }

    public Long getId() { return id; }
    public Long getLobbyId() { return lobbyId; }
    public Long getUserId() { return userId; }
    public Long getLastReadId() { return lastReadId; }
}
