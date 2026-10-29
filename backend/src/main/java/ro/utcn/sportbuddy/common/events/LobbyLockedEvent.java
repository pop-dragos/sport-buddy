package ro.utcn.sportbuddy.common.events;

import java.util.List;

// Lobby-ul a fost confirmat. finalPrice e în lei, null pentru terenurile gratuite.
public record LobbyLockedEvent(Long lobbyId, Integer finalPrice, List<Long> participantUserIds) {}
