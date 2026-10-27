package ro.utcn.sportbuddy.users.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_sports",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "sport_id"}))
public class UserSport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long sportId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SportLevel declaredLevel;

    protected UserSport() {}

    public UserSport(Long userId, Long sportId, SportLevel declaredLevel) {
        this.userId = userId;
        this.sportId = sportId;
        this.declaredLevel = declaredLevel;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getSportId() { return sportId; }
    public SportLevel getDeclaredLevel() { return declaredLevel; }
    public void setDeclaredLevel(SportLevel declaredLevel) { this.declaredLevel = declaredLevel; }
}