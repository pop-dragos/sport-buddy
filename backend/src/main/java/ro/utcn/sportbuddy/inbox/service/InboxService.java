package ro.utcn.sportbuddy.inbox.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.chat.model.LobbyMessage;
import ro.utcn.sportbuddy.chat.service.ChatService;
import ro.utcn.sportbuddy.common.i18n.Messages;
import ro.utcn.sportbuddy.common.i18n.RequestLanguage;
import ro.utcn.sportbuddy.friends.model.DirectMessage;
import ro.utcn.sportbuddy.inbox.dto.ConversationDto;
import ro.utcn.sportbuddy.inbox.repository.InboxLobbyRepository;
import ro.utcn.sportbuddy.inbox.repository.InboxRepository;
import ro.utcn.sportbuddy.lobbies.dto.LobbySummary;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.users.dto.PublicProfile;
import ro.utcn.sportbuddy.users.service.FriendshipChecker;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

// Căsuța de mesaje: conversațiile directe și chat-urile lobby-urilor într-un singur loc, cea mai recentă prima.
@Service
public class InboxService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");

    private final InboxRepository directMessages;
    private final InboxLobbyRepository lobbyMessages;
    private final UserService users;
    private final FriendshipChecker friendships;
    private final LobbyService lobbies;
    private final ChatService chat;

    public InboxService(InboxRepository directMessages, InboxLobbyRepository lobbyMessages, UserService users,
                        FriendshipChecker friendships, LobbyService lobbies, ChatService chat) {
        this.directMessages = directMessages;
        this.lobbyMessages = lobbyMessages;
        this.users = users;
        this.friendships = friendships;
        this.lobbies = lobbies;
        this.chat = chat;
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> conversations(Long me) {
        List<ConversationDto> result = new ArrayList<>(direct(me));
        result.addAll(groups(me));
        result.sort(Comparator.comparing(ConversationDto::sentAt).reversed());
        return result;
    }

    // Doar conversațiile cu prieteni actuali: mesajele directe se pot deschide doar între prieteni,
    // iar conturile șterse dispar (findProfiles întoarce doar utilizatori activi).
    private List<ConversationDto> direct(Long me) {
        List<DirectMessage> last = directMessages.findLastPerConversation(me);
        List<Long> otherIds = last.stream().map(m -> other(m, me)).toList();
        Map<Long, PublicProfile> profiles = users.findProfiles(otherIds);
        Map<Long, Integer> unread = unreadBySender(me);

        List<ConversationDto> result = new ArrayList<>();
        for (DirectMessage m : last) {
            Long otherId = other(m, me);
            PublicProfile p = profiles.get(otherId);
            if (p == null || !friendships.areFriends(me, otherId)) continue;
            result.add(new ConversationDto("DIRECT", otherId, p.username(), null, p.avatarUrl(), null, null, null,
                    m.getText(), m.getSenderId().equals(me), local(m.getSentAt()), unread.getOrDefault(otherId, 0)));
        }
        return result;
    }

    // Chat-urile lobby-urilor în care sunt participant și în care s-a scris cel puțin un mesaj.
    private List<ConversationDto> groups(Long me) {
        Set<Long> lobbyIds = lobbies.participatingLobbyIds(me);
        if (lobbyIds.isEmpty()) return List.of();

        List<LobbyMessage> last = lobbyMessages.findLastPerLobby(lobbyIds);
        if (last.isEmpty()) return List.of();

        Map<Long, LobbySummary> summaries = lobbies.summariesOf(
                last.stream().map(LobbyMessage::getLobbyId).toList(), me).stream()
                .collect(Collectors.toMap(LobbySummary::id, s -> s));
        Map<Long, PublicProfile> senders = users.findProfiles(last.stream()
                .map(LobbyMessage::getSenderId).filter(Objects::nonNull).distinct().toList());
        Map<Long, Long> unread = chat.unreadByLobby(me);

        List<ConversationDto> result = new ArrayList<>();
        for (LobbyMessage m : last) {
            LobbySummary lobby = summaries.get(m.getLobbyId());
            if (lobby == null) continue;
            PublicProfile sender = m.getSenderId() == null ? null : senders.get(m.getSenderId());
            boolean fromMe = me.equals(m.getSenderId());
            // mesajele de sistem se salvează în română; la citire se traduc după limba cererii
            String text = m.isSystem() ? Messages.translate(m.getText(), RequestLanguage.current()) : m.getText();
            result.add(new ConversationDto("GROUP", lobby.id(), lobby.sportName(), lobby.fieldName(), null,
                    lobby.sportCode(), lobby.dateTime(), sender == null ? null : sender.username(), text, fromMe,
                    local(m.getSentAt()), unread.getOrDefault(lobby.id(), 0L).intValue()));
        }
        return result;
    }

    // totalul de mesaje necitite (directe și din grupuri), pentru insigna de pe iconița din header
    @Transactional(readOnly = true)
    public long unreadCount(Long me) {
        long groups = chat.unreadByLobby(me).values().stream().mapToLong(Long::longValue).sum();
        return directMessages.countUnread(me) + groups;
    }

    private Map<Long, Integer> unreadBySender(Long me) {
        Map<Long, Integer> unread = new HashMap<>();
        for (Object[] row : directMessages.countUnreadBySender(me)) {
            unread.put((Long) row[0], ((Long) row[1]).intValue());
        }
        return unread;
    }

    private static Long other(DirectMessage m, Long me) {
        return m.getSenderId().equals(me) ? m.getRecipientId() : m.getSenderId();
    }

    private static LocalDateTime local(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZONE);
    }
}
