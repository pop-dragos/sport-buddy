package ro.utcn.sportbuddy.common.events;

// accepterId a acceptat cererea de prietenie trimisă de requesterId (sau au trimis amândoi, deci sunt prieteni).
public record FriendAcceptedEvent(Long requesterId, Long accepterId) {}
