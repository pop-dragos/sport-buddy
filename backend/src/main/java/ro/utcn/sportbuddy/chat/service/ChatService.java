package ro.utcn.sportbuddy.chat.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ro.utcn.sportbuddy.chat.dto.ChatMessageDto;
import ro.utcn.sportbuddy.chat.model.LobbyChatRead;
import ro.utcn.sportbuddy.chat.model.LobbyMessage;
import ro.utcn.sportbuddy.chat.repository.LobbyChatReadRepository;
import ro.utcn.sportbuddy.chat.repository.LobbyMessageRepository;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.common.i18n.Messages;
import ro.utcn.sportbuddy.common.i18n.RequestLanguage;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");

    private final LobbyMessageRepository messageRepository;
    private final LobbyChatReadRepository readRepository;
    private final LobbyService lobbyService;
    private final UserService userService;

    public ChatService(LobbyMessageRepository messageRepository, LobbyChatReadRepository readRepository,
                       LobbyService lobbyService, UserService userService) {
        this.messageRepository = messageRepository;
        this.readRepository = readRepository;
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

        // cine citește mesajele le are citite: avansăm marcajul până la ultimul primit
        if (!messages.isEmpty()) {
            markRead(lobbyId, userId, messages.get(messages.size() - 1).getId());
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

    // Câte mesaje necitite (scrise de alții, fără cele de sistem) are utilizatorul în fiecare lobby al lui.
    // Apar doar lobby-urile cu cel puțin unul necitit. Cine nu a deschis niciodată chat-ul le are pe toate necitite.
    public Map<Long, Long> unreadByLobby(Long userId) {
        Set<Long> lobbyIds = lobbyService.participatingLobbyIds(userId);
        if (lobbyIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> lastRead = readRepository.findByUserIdAndLobbyIdIn(userId, lobbyIds).stream()
                .collect(Collectors.toMap(LobbyChatRead::getLobbyId, LobbyChatRead::getLastReadId));

        Map<Long, Long> unread = new LinkedHashMap<>();
        for (Long lobbyId : lobbyIds) {
            long count = messageRepository.countByLobbyIdAndSystemFalseAndSenderIdNotAndIdGreaterThan(
                    lobbyId, userId, lastRead.getOrDefault(lobbyId, 0L));
            if (count > 0) {
                unread.put(lobbyId, count);
            }
        }
        return unread;
    }

    private void markRead(Long lobbyId, Long userId, Long lastMessageId) {
        LobbyChatRead read = readRepository.findByLobbyIdAndUserId(lobbyId, userId)
                .orElseGet(() -> new LobbyChatRead(lobbyId, userId, 0L));
        read.advanceTo(lastMessageId);
        readRepository.save(read);
    }

    // Mesajele de sistem („X s-a înscris", „Lobby confirmat"), scrise de ChatEventListener.
    // Nu verifică participantul: le scrie aplicația, nu un utilizator.
    public void addSystemMessage(Long lobbyId, String text) {
        messageRepository.save(new LobbyMessage(lobbyId, null, true, text));
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
                // mesajele de sistem se salvează în română; la citire se traduc după limba cererii
                m.isSystem() ? Messages.translate(m.getText(), RequestLanguage.current()) : m.getText(),
                LocalDateTime.ofInstant(m.getSentAt(), ZONE));
    }
}
