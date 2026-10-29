package ro.utcn.sportbuddy.feed.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "posts", indexes = @Index(columnList = "sportId, createdAt"))
public class Post {

    public static final int MAX_TEXT = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private Long sportId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostTag tag;

    @Column(nullable = false, length = MAX_TEXT)
    private String text;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Post() {}

    public Post(Long authorId, Long sportId, PostTag tag, String text) {
        this.authorId = authorId;
        this.sportId = sportId;
        this.tag = tag;
        this.text = text;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public Long getSportId() { return sportId; }
    public PostTag getTag() { return tag; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }

    // doar pentru datele demo din seeder, ca postările să nu aibă toate aceeași oră
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
