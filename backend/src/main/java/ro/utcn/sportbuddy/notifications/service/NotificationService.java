package ro.utcn.sportbuddy.notifications.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.notifications.dto.NotificationDto;
import ro.utcn.sportbuddy.notifications.model.Notification;
import ro.utcn.sportbuddy.notifications.model.NotificationType;
import ro.utcn.sportbuddy.notifications.repository.NotificationRepository;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class NotificationService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");

    private final NotificationRepository repository;
    private final UserService userService;
    private final LobbyService lobbyService;

    public NotificationService(NotificationRepository repository, UserService userService,
                               LobbyService lobbyService) {
        this.repository = repository;
        this.userService = userService;
        this.lobbyService = lobbyService;
    }

    // Creează notificarea pentru userId. Nu face nimic dacă contul nu mai e activ, dacă destinatarul e chiar cel
    // care a făcut acțiunea, sau dacă (la cererile de alăturare) și-a oprit notificările din setări.
    @Transactional
    public void notify(Long userId, NotificationType type, Long lobbyId, Long actorId) {
        if (userId.equals(actorId) || !userService.isActive(userId)) {
            return;
        }
        if (type == NotificationType.JOIN_REQUESTED && !userService.getSettings(userId).notifyJoinRequests()) {
            return;
        }
        String actorName = actorId == null ? null : userService.getSummary(actorId).username();
        String subject = lobbyId == null ? null : lobbyService.sportNameOf(lobbyId);
        repository.save(new Notification(userId, type, lobbyId, actorId, actorName, subject));
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> list(Long userId) {
        return repository.findTop50ByUserIdOrderByIdDesc(userId).stream().map(NotificationService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return repository.countByUserIdAndReadAtIsNull(userId);
    }

    // Doar a mea: o notificare a altcuiva răspunde ca și cum n-ar exista.
    @Transactional
    public void markRead(Long userId, Long id) {
        Notification n = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Notificarea nu există."));
        n.markRead(Instant.now());
    }

    @Transactional
    public void markAllRead(Long userId) {
        Instant now = Instant.now();
        repository.findByUserIdAndReadAtIsNull(userId).forEach(n -> n.markRead(now));
    }

    private static NotificationDto toDto(Notification n) {
        return new NotificationDto(n.getId(), n.getType().name(), n.getLobbyId(), n.getActorId(), n.getActorName(),
                n.getSubject(), LocalDateTime.ofInstant(n.getCreatedAt(), ZONE), n.getReadAt() != null);
    }
}
