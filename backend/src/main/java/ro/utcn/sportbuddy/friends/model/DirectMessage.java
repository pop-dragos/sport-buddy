package ro.utcn.sportbuddy.friends.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "direct_messages", indexes = @Index(columnList = "sender_id, recipient_id"))
public class DirectMessage {

    public static final int MAX_TEXT = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false, length = MAX_TEXT)
    private String text;

    @Column(nullable = false, updatable = false)
    private Instant sentAt;

    // null până când destinatarul deschide conversația
    private Instant readAt;

    protected DirectMessage() {}

    public DirectMessage(Long senderId, Long recipientId, String text) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.text = text;
    }

    @PrePersist
    void onCreate() {
        if (sentAt == null) sentAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getSenderId() { return senderId; }
    public Long getRecipientId() { return recipientId; }
    public String getText() { return text; }
    public Instant getSentAt() { return sentAt; }
    public Instant getReadAt() { return readAt; }

    // doar pentru datele demo din seeder
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}
