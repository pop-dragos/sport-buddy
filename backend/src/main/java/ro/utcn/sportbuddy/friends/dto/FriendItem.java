package ro.utcn.sportbuddy.friends.dto;

import java.util.List;

// Rând din lista de prieteni, din sugestii și din cereri.
// matchesTogether rămâne null până când modulul lobbies oferă meciurile jucate împreună.
public record FriendItem(Long userId, String username, String fullName, String avatarUrl, String cityName,
                         List<String> sportCodes, Integer matchesTogether, Double rating) {}
