package ro.utcn.sportbuddy.feed.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "comments", indexes = @Index(columnList = "postId"))
public class Comment {

    public static final int MAX_TEXT = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, length = MAX_TEXT)
    private String text;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Comment() {}

    public Comment(Long postId, Long authorId, String text) {
        this.postId = postId;
        this.authorId = authorId;
        this.text = text;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }
}
