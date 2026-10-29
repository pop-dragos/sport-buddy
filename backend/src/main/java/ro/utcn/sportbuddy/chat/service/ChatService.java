package ro.utcn.sportbuddy.chat.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ro.utcn.sportbuddy.chat.dto.ChatMessageDto;
import ro.utcn.sportbuddy.chat.model.LobbyMessage;
import ro.utcn.sportbuddy.chat.repository.LobbyMessageRepository;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");

    private final LobbyMessageRepository messageRepository;
    private final LobbyService lobbyService;
    private final UserService userService;

    public ChatService(LobbyMessageRepository messageRepository, LobbyService lobbyService,
                       UserService userService) {
        this.messageRepository = messageRepository;
        this.lobbyService = lobbyService;
        this.userService = userService;
    }

    // Fără afterId: ultimele 100 de mesaje. Cu afterId: doar cele mai noi decât el.
    public List<ChatMessageDto> getMessages(Long lobbyId, Long userId, Long afterId) {
        requireParticipant(lobbyId, userId);

        List<LobbyMessage> messages;
        if (afterId == null) {
            messages = new ArrayList<>(messageRepository.findTop100ByLobbyIdOrderByIdDesc(lobbyId));
            Collections.reverse(messages);
        } else {
            messages = messageRepository.findByLobbyIdAndIdGreaterThanOrderByIdAsc(lobbyId, afterId);
        }

        // un singur apel la UserService pentru fiecare autor, nu pentru fiecare mesaj
        Map<Long, String> usernames = new HashMap<>();
        return messages.stream().map(m -> toDto(m, usernames)).toList();
    }

    public ChatMessageDto send(Long lobbyId, Long userId, String text) {
        requireParticipant(lobbyId, userId);
        LobbyMessage saved = messageRepository.save(new LobbyMessage(lobbyId, userId, false, text.trim()));
        return toDto(saved, new HashMap<>());
    }

    // Rulează la fiecare interogare (3 secunde), deci întâi verificarea ieftină.
    // Doar pentru cei care nu sunt participanți cerem lobby-ul, ca să răspundem 404 dacă nu există.
    private void requireParticipant(Long lobbyId, Long userId) {
        if (lobbyService.isParticipant(lobbyId, userId)) {
            return;
        }
        lobbyService.getDetails(lobbyId, userId);
        throw new ApiException(HttpStatus.FORBIDDEN, "NOT_PARTICIPANT",
                "Doar participanții lobby-ului pot folosi chat-ul.");
    }

    private ChatMessageDto toDto(LobbyMessage m, Map<Long, String> usernames) {
        String username = m.isSystem()
                ? null
                : usernames.computeIfAbsent(m.getSenderId(), id -> userService.getSummary(id).username());
        return new ChatMessageDto(
                m.getId(),
                m.isSystem() ? "SYSTEM" : "USER",
                m.isSystem() ? null : m.getSenderId(),
                username,
                m.getText(),
                LocalDateTime.ofInstant(m.getSentAt(), ZONE));
    }
}
