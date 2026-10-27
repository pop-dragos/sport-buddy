package ro.utcn.sportbuddy.lobbies.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "lobbies")
public class Lobby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private Long sportId;

    @Column(nullable = false)
    private Long cityId;

    @Column(nullable = false)
    private Long fieldId;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer minPlayers;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fieldCost;

    @Column(nullable = false)
    private Integer guestCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LobbyStatus status = LobbyStatus.OPEN;

    @Column(nullable = false)
    private LocalDateTime lockDeadline;

    private LocalDateTime lockedAt;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalPricePerPlayer;

    @Column(nullable = false)
    private boolean attendanceSaved;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(Integer minPlayers) {
        this.minPlayers = minPlayers;
    }

    public BigDecimal getFieldCost() {
        return fieldCost;
    }

    public void setFieldCost(BigDecimal fieldCost) {
        this.fieldCost = fieldCost;
    }

    public Integer getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(Integer guestCount) {
        this.guestCount = guestCount;
    }

    public LobbyStatus getStatus() {
        return status;
    }

    public void setStatus(LobbyStatus status) {
        this.status = status;
    }

    public LocalDateTime getLockDeadline() {
        return lockDeadline;
    }

    public void setLockDeadline(LocalDateTime lockDeadline) {
        this.lockDeadline = lockDeadline;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public BigDecimal getFinalPricePerPlayer() {
        return finalPricePerPlayer;
    }

    public void setFinalPricePerPlayer(BigDecimal finalPricePerPlayer) {
        this.finalPricePerPlayer = finalPricePerPlayer;
    }

    public boolean isAttendanceSaved() {
        return attendanceSaved;
    }

    public void setAttendanceSaved(boolean attendanceSaved) {
        this.attendanceSaved = attendanceSaved;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
