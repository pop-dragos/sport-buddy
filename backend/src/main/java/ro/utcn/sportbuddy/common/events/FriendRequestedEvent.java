package ro.utcn.sportbuddy.common.events;

// requesterId i-a trimis o cerere de prietenie lui addresseeId.
public record FriendRequestedEvent(Long requesterId, Long addresseeId) {}
