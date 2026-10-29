package ro.utcn.sportbuddy.friends.dto;

import java.util.List;

public record FriendRequests(List<FriendRequestItem> incoming, List<FriendRequestItem> outgoing) {}
