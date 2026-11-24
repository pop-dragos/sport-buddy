package ro.utcn.sportbuddy.notifications.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.notifications.dto.NotificationDto;
import ro.utcn.sportbuddy.notifications.model.Notification;
import ro.utcn.sportbuddy.notifications.model.NotificationType;
import ro.utcn.sportbuddy.notifications.repository.NotificationRepository;
import ro.utcn.sportbuddy.users.dto.UserSettingsDto;
import ro.utcn.sportbuddy.users.dto.UserSummary;
import ro.utcn.sportbuddy.users.model.ProfileVisibility;
import ro.utcn.sportbuddy.users.model.Theme;
import ro.utcn.sportbuddy.users.model.WhoCanAddMe;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Repository-ul și celelalte servicii sunt simulate, deci testul nu are nevoie de bază de date.
class NotificationServiceTest {

    private static final Long ANA = 1L, ANDREI = 2L;
    private static final Long LOBBY = 55L;

    private NotificationRepository repository;
    private UserService users;
    private LobbyService lobbies;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationRepository.class);
        users = mock(UserService.class);
        lobbies = mock(LobbyService.class);
        service = new NotificationService(repository, users, lobbies);

        when(users.isActive(any())).thenReturn(true);
        when(users.getSettings(any())).thenReturn(settings(true));
        when(users.getSummary(ANDREI)).thenReturn(new UserSummary(ANDREI, "andrei", "Andrei Ionescu", "andrei@ex.ro"));
        when(lobbies.sportNameOf(LOBBY)).thenReturn("Fotbal");
    }

    @Test
    void joinRequestNotifiesTheHostWithWhoAndWhere() {
        service.notify(ANA, NotificationType.JOIN_REQUESTED, LOBBY, ANDREI);

        verify(repository).save(argThat(n -> n.getUserId().equals(ANA)
                && n.getType() == NotificationType.JOIN_REQUESTED
                && n.getLobbyId().equals(LOBBY)
                && n.getActorId().equals(ANDREI)
                && "andrei".equals(n.getActorName())
                && "Fotbal".equals(n.getSubject())));
    }

    @Test
    void joinRequestRespectsTheSetting() {
        when(users.getSettings(ANA)).thenReturn(settings(false));

        service.notify(ANA, NotificationType.JOIN_REQUESTED, LOBBY, ANDREI);

        verify(repository, never()).save(any());
    }

    @Test
    void otherTypesIgnoreTheJoinRequestSetting() {
        when(users.getSettings(ANA)).thenReturn(settings(false));

        service.notify(ANA, NotificationType.LOBBY_LOCKED, LOBBY, null);

        verify(repository).save(any());
    }

    @Test
    void nobodyIsNotifiedAboutTheirOwnAction() {
        service.notify(ANA, NotificationType.LOBBY_CANCELLED, LOBBY, ANA);

        verify(repository, never()).save(any());
    }

    @Test
    void inactiveAccountsGetNothing() {
        when(users.isActive(ANA)).thenReturn(false);

        service.notify(ANA, NotificationType.LOBBY_LOCKED, LOBBY, null);

        verify(repository, never()).save(any());
    }

    @Test
    void friendNotificationsHaveNoLobbyAndNoSport() {
        service.notify(ANA, NotificationType.FRIEND_REQUEST, null, ANDREI);

        verify(repository).save(argThat(n -> n.getLobbyId() == null && n.getSubject() == null));
        verifyNoInteractions(lobbies);
    }

    @Test
    void listMapsReadState() {
        Notification unread = notification(2L, ANA, NotificationType.LOBBY_LOCKED);
        Notification read = notification(1L, ANA, NotificationType.FRIEND_REQUEST);
        read.markRead(Instant.now());
        when(repository.findTop50ByUserIdOrderByIdDesc(ANA)).thenReturn(List.of(unread, read));

        List<NotificationDto> list = service.list(ANA);

        assertEquals(List.of(2L, 1L), list.stream().map(NotificationDto::id).toList());
        assertFalse(list.get(0).read());
        assertTrue(list.get(1).read());
        assertEquals("LOBBY_LOCKED", list.get(0).type());
    }

    @Test
    void markReadSetsTheTimestampOnce() {
        Notification n = notification(7L, ANA, NotificationType.LOBBY_LOCKED);
        when(repository.findByIdAndUserId(7L, ANA)).thenReturn(Optional.of(n));

        service.markRead(ANA, 7L);
        Instant first = n.getReadAt();
        service.markRead(ANA, 7L);

        assertNotNull(first);
        assertEquals(first, n.getReadAt());
    }

    @Test
    void someoneElsesNotificationIsNotFound() {
        when(repository.findByIdAndUserId(7L, ANDREI)).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class, () -> service.markRead(ANDREI, 7L));

        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    void markAllReadTouchesOnlyUnreadOnes() {
        Notification a = notification(1L, ANA, NotificationType.LOBBY_LOCKED);
        Notification b = notification(2L, ANA, NotificationType.FRIEND_REQUEST);
        when(repository.findByUserIdAndReadAtIsNull(ANA)).thenReturn(List.of(a, b));

        service.markAllRead(ANA);

        assertNotNull(a.getReadAt());
        assertNotNull(b.getReadAt());
    }

    @Test
    void unreadCountComesFromTheRepository() {
        when(repository.countByUserIdAndReadAtIsNull(ANA)).thenReturn(4L);

        assertEquals(4L, service.unreadCount(ANA));
    }

    private static UserSettingsDto settings(boolean notifyJoinRequests) {
        return new UserSettingsDto(Theme.LIGHT, true, notifyJoinRequests, true, true, false,
                ProfileVisibility.EVERYONE, WhoCanAddMe.EVERYONE, true);
    }

    // Notificările din teste nu trec prin bază de date, deci id-ul și data li se pun de mână.
    private static Notification notification(Long id, Long userId, NotificationType type) {
        Notification n = new Notification(userId, type, null, null, null, null);
        ReflectionTestUtils.setField(n, "id", id);
        ReflectionTestUtils.setField(n, "createdAt", Instant.now());
        return n;
    }
}
