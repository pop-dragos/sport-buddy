package ro.utcn.sportbuddy.friends.dto;

import java.util.List;

// Rând din lista de prieteni, din sugestii și din cereri.
// matchesTogether: câte meciuri încheiate am jucat împreună (din lobbies).
public record FriendItem(Long userId, String username, String fullName, String avatarUrl, String cityName,
                         List<String> sportCodes, Integer matchesTogether, Double rating) {}
