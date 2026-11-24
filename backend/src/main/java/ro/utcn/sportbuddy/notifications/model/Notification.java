package ro.utcn.sportbuddy.notifications.model;

import jakarta.persistence.*;

import java.time.Instant;

// O notificare în aplicație. Textul îl compune frontend-ul din tip și câmpuri (ca să respecte limba aleasă),
// de aceea aici se păstrează doar datele: cine a făcut ce și la ce lobby.
@Entity
@Table(name = "notifications", indexes = @Index(columnList = "user_id, id"))
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    // lobby-ul la care duce notificarea; null la prietenii
    private Long lobbyId;

    // cine a declanșat-o (null când vine de la aplicație, de exemplu „lobby confirmat")
    private Long actorId;
    private String actorName;

    // sportul lobby-ului, așa cum e în catalog (se traduce în frontend)
    private String subject;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // null până când utilizatorul o citește
    private Instant readAt;

    protected Notification() {}

    public Notification(Long userId, NotificationType type, Long lobbyId, Long actorId, String actorName,
                        String subject) {
        this.userId = userId;
        this.type = type;
        this.lobbyId = lobbyId;
        this.actorId = actorId;
        this.actorName = actorName;
        this.subject = subject;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public void markRead(Instant when) {
        if (readAt == null) readAt = when;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public NotificationType getType() { return type; }
    public Long getLobbyId() { return lobbyId; }
    public Long getActorId() { return actorId; }
    public String getActorName() { return actorName; }
    public String getSubject() { return subject; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadAt() { return readAt; }
}
