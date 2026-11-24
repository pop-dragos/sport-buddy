package ro.utcn.sportbuddy.friends.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

// Rând din lista de prieteni, din sugestii și din cereri.
// matchesTogether: câte meciuri încheiate am jucat împreună (din lobbies).
// unreadMessages: câte mesaje necitite am de la el; vine doar în lista mea de prieteni.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FriendItem(Long userId, String username, String fullName, String avatarUrl, String cityName,
                         List<String> sportCodes, Integer matchesTogether, Double rating, Integer unreadMessages) {

    public FriendItem(Long userId, String username, String fullName, String avatarUrl, String cityName,
                      List<String> sportCodes, Integer matchesTogether, Double rating) {
        this(userId, username, fullName, avatarUrl, cityName, sportCodes, matchesTogether, rating, null);
    }

    public FriendItem withUnreadMessages(int count) {
        return new FriendItem(userId, username, fullName, avatarUrl, cityName, sportCodes, matchesTogether,
                rating, count);
    }
}
