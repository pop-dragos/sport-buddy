package ro.utcn.sportbuddy.users.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import ro.utcn.sportbuddy.catalog.dto.CityDto;
import ro.utcn.sportbuddy.catalog.dto.SportDto;
import ro.utcn.sportbuddy.catalog.service.CatalogService;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.users.dto.SportLevelDto;
import ro.utcn.sportbuddy.users.dto.UpdateProfileRequest;
import ro.utcn.sportbuddy.users.dto.UserMe;
import ro.utcn.sportbuddy.users.dto.UserSettingsDto;
import ro.utcn.sportbuddy.users.model.*;
import ro.utcn.sportbuddy.users.repository.UserRepository;
import ro.utcn.sportbuddy.users.repository.UserSettingsRepository;
import ro.utcn.sportbuddy.users.repository.UserSportRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository users;
    private UserSportRepository userSports;
    private UserSettingsRepository settings;
    private CatalogService catalog;
    private PasswordEncoder encoder;
    private AvatarStorage avatars;
    private boolean friends;

    @TempDir
    java.nio.file.Path uploads;
    private UserService service;

    private User ana;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        userSports = mock(UserSportRepository.class);
        settings = mock(UserSettingsRepository.class);
        catalog = mock(CatalogService.class);
        encoder = new BCryptPasswordEncoder();
        avatars = new AvatarStorage(uploads.toString());
        friends = false;
        @SuppressWarnings("unchecked")
        org.springframework.beans.factory.ObjectProvider<FriendshipChecker> checker =
                mock(org.springframework.beans.factory.ObjectProvider.class);
        when(checker.getIfAvailable()).thenReturn((a, b) -> friends);
        service = new UserService(users, userSports, settings, encoder, catalog, avatars, checker);

        ana = user(1L, "ana", "ana@demo.ro");
        when(users.findById(1L)).thenReturn(Optional.of(ana));
        when(userSports.findByUserId(1L)).thenReturn(List.of());
        when(catalog.findSport(any())).thenReturn(Optional.of(mock(SportDto.class)));
        // ca repository-ul real: save întoarce entitatea salvată
        when(settings.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private static User user(Long id, String username, String email) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        ReflectionTestUtils.setField(u, "createdAt", Instant.now());
        u.setUsername(username);
        u.setFullName("Ana Popescu");
        u.setEmail(email);
        u.setAge(21);
        return u;
    }

    private static UpdateProfileRequest profile(String fullName, String email, String phone, Integer age,
                                                Long cityId, String description) {
        return new UpdateProfileRequest(fullName, email, phone, age, cityId, description);
    }

    // ---------- profil ----------

    @Test
    void updateMeChangesOnlySentFields() {
        ana.setPhone("0722123456");

        UserMe me = service.updateMe(1L, profile("Ana Maria", null, null, 22, null, null));

        assertEquals("Ana Maria", me.fullName());
        assertEquals(22, me.age());
        assertEquals("0722123456", me.phone());
        assertEquals("ana@demo.ro", me.email());
    }

    @Test
    void updateMeClearsPhoneAndDescriptionWhenSentEmpty() {
        ana.setPhone("0722123456");
        ana.setBio("Joc în weekend.");

        UserMe me = service.updateMe(1L, profile(null, null, "", null, null, "  "));

        assertNull(me.phone());
        assertNull(me.description());
    }

    @Test
    void updateMeRejectsEmailUsedByAnotherUser() {
        when(users.findByEmailIgnoreCase("bogdan@demo.ro"))
                .thenReturn(Optional.of(user(2L, "bogdan", "bogdan@demo.ro")));

        ApiException e = assertThrows(ApiException.class,
                () -> service.updateMe(1L, profile(null, "bogdan@demo.ro", null, null, null, null)));

        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("EMAIL_TAKEN", e.getCode());
        assertEquals("ana@demo.ro", ana.getEmail());
    }

    @Test
    void updateMeAllowsKeepingOwnEmail() {
        when(users.findByEmailIgnoreCase("ana@demo.ro")).thenReturn(Optional.of(ana));

        UserMe me = service.updateMe(1L, profile(null, "ana@demo.ro", null, null, null, null));

        assertEquals("ana@demo.ro", me.email());
    }

    @Test
    void updateMeRejectsBlankName() {
        ApiException e = assertThrows(ApiException.class,
                () -> service.updateMe(1L, profile("  ", null, null, null, null, null)));
        assertEquals("VALIDATION_ERROR", e.getCode());
        assertTrue(e.getFields().containsKey("fullName"));
    }

    @Test
    void updateMeRejectsUnknownCity() {
        when(catalog.findCity(99L)).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class,
                () -> service.updateMe(1L, profile(null, null, null, null, 99L, null)));

        assertTrue(e.getFields().containsKey("cityId"));
    }

    @Test
    void updateMeSetsKnownCity() {
        when(catalog.findCity(4L)).thenReturn(Optional.of(mock(CityDto.class)));

        UserMe me = service.updateMe(1L, profile(null, null, null, null, 4L, null));

        assertEquals(4L, me.cityId());
    }

    // ---------- sporturi ----------

    @Test
    void updateSportsRemovesMissingChangesLevelAndAddsNew() {
        UserSport keep = new UserSport(1L, 1L, SportLevel.BEGINNER);
        UserSport drop = new UserSport(1L, 2L, SportLevel.BEGINNER);
        when(userSports.findByUserId(1L)).thenReturn(new ArrayList<>(List.of(keep, drop)));

        service.updateSports(1L, List.of(
                new SportLevelDto(1L, SportLevel.ADVANCED),
                new SportLevelDto(3L, SportLevel.INTERMEDIATE)));

        assertEquals(SportLevel.ADVANCED, keep.getDeclaredLevel());
        verify(userSports).delete(drop);
        verify(userSports, never()).delete(keep);
        verify(userSports).save(argThat(s -> s.getSportId().equals(3L)
                && s.getDeclaredLevel() == SportLevel.INTERMEDIATE));
    }

    @Test
    void updateSportsRequiresAtLeastOneSport() {
        ApiException e = assertThrows(ApiException.class, () -> service.updateSports(1L, List.of()));
        assertEquals("VALIDATION_ERROR", e.getCode());
        assertTrue(e.getFields().containsKey("sports"));
        verify(userSports, never()).delete(any());
    }

    @Test
    void updateSportsRejectsSportMissingFromCatalog() {
        when(catalog.findSport(99L)).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class,
                () -> service.updateSports(1L, List.of(new SportLevelDto(99L, SportLevel.BEGINNER))));

        assertTrue(e.getFields().containsKey("sports"));
    }

    @Test
    void createUserWithUnknownSportSavesNothing() {
        when(catalog.findSport(99L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> service.createUser("Test", "t@ex.ro", "hash", 20, null, null,
                null, AuthProvider.LOCAL, List.of(new SportLevelDto(99L, SportLevel.BEGINNER))));

        verify(users, never()).save(any());
    }

    // ---------- parolă ----------

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        ana.setPasswordHash(encoder.encode("demo123"));

        ApiException e = assertThrows(ApiException.class,
                () -> service.changePassword(1L, "gresita", "parolaNoua"));

        assertEquals("WRONG_PASSWORD", e.getCode());
        assertTrue(encoder.matches("demo123", ana.getPasswordHash()));
    }

    @Test
    void changePasswordStoresNewHash() {
        ana.setPasswordHash(encoder.encode("demo123"));

        service.changePassword(1L, "demo123", "parolaNoua");

        assertTrue(encoder.matches("parolaNoua", ana.getPasswordHash()));
        assertNotEquals("parolaNoua", ana.getPasswordHash());
    }

    @Test
    void socialAccountWithoutPasswordCannotChangeIt() {
        ana.setPasswordHash(null);
        ApiException e = assertThrows(ApiException.class,
                () -> service.changePassword(1L, "orice", "parolaNoua"));
        assertEquals("WRONG_PASSWORD", e.getCode());
    }

    // ---------- ștergere cont ----------

    @Test
    void deleteMeDeactivatesAndAnonymizes() {
        ana.setPasswordHash("hash");
        ana.setPhone("0722123456");
        ana.setBio("Joc în weekend.");
        ana.setCityId(4L);

        service.deleteMe(1L);

        assertFalse(ana.isActive());
        assertEquals("sters-1", ana.getUsername());
        assertEquals("sters-1@sters.invalid", ana.getEmail());
        assertEquals("Utilizator șters", ana.getFullName());
        assertNull(ana.getPasswordHash());
        assertNull(ana.getPhone());
        assertNull(ana.getBio());
        assertNull(ana.getCityId());
        verify(userSports).deleteByUserId(1L);
        assertFalse(service.isActive(1L));
        assertTrue(service.findCredentials("ana").isEmpty());
    }

    @Test
    void publicProfileOfDeletedUserIsNotFound() {
        ana.setActive(false);
        ApiException e = assertThrows(ApiException.class, () -> service.getPublicProfile(2L, 1L));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    void publicProfileHidesEmailAndPhone() {
        ana.setPhone("0722123456");
        var profile = service.getPublicProfile(2L, 1L);
        assertEquals("ana", profile.username());
        assertTrue(java.util.Arrays.stream(profile.getClass().getRecordComponents())
                .noneMatch(c -> c.getName().equals("email") || c.getName().equals("phone")));
    }

    // ---------- setări ----------

    @Test
    void getSettingsCreatesDefaultsForExistingUser() {
        when(settings.findById(1L)).thenReturn(Optional.empty());
        when(settings.save(any())).thenAnswer(i -> i.getArgument(0));

        UserSettingsDto dto = service.getSettings(1L);

        assertEquals(Theme.SYSTEM, dto.theme());
        assertTrue(dto.showWeather());
        assertFalse(dto.notifyWeeklyDigest());
        assertEquals(WhoCanAddMe.EVERYONE, dto.whoCanAddMe());
    }

    @Test
    void updateSettingsSavesAllFields() {
        UserSettings existing = new UserSettings(1L);
        when(settings.findById(1L)).thenReturn(Optional.of(existing));

        UserSettingsDto saved = service.updateSettings(1L, new UserSettingsDto(Theme.DARK, false, false,
                true, false, true, ProfileVisibility.FRIENDS, WhoCanAddMe.NOBODY, false));

        assertEquals(Theme.DARK, existing.getTheme());
        assertFalse(existing.isShowWeather());
        assertTrue(existing.isNotifEmailDigest());
        assertEquals(ProfileVisibility.FRIENDS, existing.getProfileVisibility());
        assertEquals(WhoCanAddMe.NOBODY, existing.getWhoCanAddMe());
        assertFalse(existing.isShowFriendList());
        assertEquals(Theme.DARK, saved.theme());
    }

    // ---------- completare profil ----------

    @Test
    void profileCompletionGrowsWithOptionalFields() {
        UserMe before = service.getMe(1L);
        assertEquals(30, before.profileCompletion().percent());

        ana.setPhone("0722123456");
        ana.setCityId(4L);
        ana.setBio("Joc în weekend.");
        UserMe after = service.getMe(1L);

        assertEquals(75, after.profileCompletion().percent());
        assertEquals(List.of("avatar"), after.profileCompletion().missing());
    }

    // ---------- poză de profil ----------

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10, 0, 0};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};

    @Test
    void avatarIsSavedAndReplacingDeletesTheOldFile() throws Exception {
        UserMe first = service.updateAvatar(1L, PNG);
        String firstName = ana.getPhotoPath();
        assertTrue(firstName.matches("1-[0-9a-f]{32}\\.png"));
        assertEquals("/api/uploads/" + firstName, first.avatarUrl());
        assertTrue(java.nio.file.Files.exists(uploads.resolve(firstName)));

        service.updateAvatar(1L, JPG);

        assertTrue(ana.getPhotoPath().endsWith(".jpg"));
        assertFalse(java.nio.file.Files.exists(uploads.resolve(firstName)));
        assertTrue(service.findAvatarFile(ana.getPhotoPath()).isPresent());
    }

    @Test
    void avatarRejectsFilesThatAreNotImages() {
        byte[] text = "nu sunt o imagine".getBytes();
        ApiException e = assertThrows(ApiException.class, () -> service.updateAvatar(1L, text));
        assertTrue(e.getFields().containsKey("file"));
        assertNull(ana.getPhotoPath());
    }

    @Test
    void avatarRejectsFilesOverTwoMegabytes() {
        byte[] big = new byte[UserService.MAX_AVATAR_BYTES + 1];
        System.arraycopy(PNG, 0, big, 0, PNG.length);
        ApiException e = assertThrows(ApiException.class, () -> service.updateAvatar(1L, big));
        assertEquals("Imaginea poate avea maximum 2 MB.", e.getFields().get("file"));
    }

    @Test
    void avatarRejectsEmptyFile() {
        assertThrows(ApiException.class, () -> service.updateAvatar(1L, new byte[0]));
    }

    @Test
    void removeAvatarDeletesFileAndCountsAsMissing() {
        service.updateAvatar(1L, PNG);
        String name = ana.getPhotoPath();

        UserMe me = service.removeAvatar(1L);

        assertNull(me.avatarUrl());
        assertTrue(me.profileCompletion().missing().contains("avatar"));
        assertFalse(java.nio.file.Files.exists(uploads.resolve(name)));
    }

    @Test
    void deleteMeAlsoDeletesAvatarFile() {
        service.updateAvatar(1L, PNG);
        String name = ana.getPhotoPath();

        service.deleteMe(1L);

        assertFalse(java.nio.file.Files.exists(uploads.resolve(name)));
    }

    @Test
    void avatarFileLookupRefusesPathTricks() {
        assertTrue(service.findAvatarFile("../application.properties").isEmpty());
        assertTrue(service.findAvatarFile("..%2F..%2Fpom.xml").isEmpty());
        assertTrue(service.findAvatarFile("1-abc.png").isEmpty());
    }

    @Test
    void imageTypeIsDetectedFromContent() {
        assertEquals(Optional.of("png"), UserService.imageExtension(PNG));
        assertEquals(Optional.of("jpg"), UserService.imageExtension(JPG));
        assertEquals(Optional.of("gif"), UserService.imageExtension("GIF89a".getBytes()));
        assertEquals(Optional.of("webp"), UserService.imageExtension("RIFF0000WEBPVP8 ".getBytes()));
        assertEquals(Optional.empty(), UserService.imageExtension("RIFF0000WAVE".getBytes()));
    }

    // ---------- vizibilitatea profilului ----------

    private void anaShowsProfileOnlyToFriends() {
        UserSettings s = new UserSettings(1L);
        s.setProfileVisibility(ProfileVisibility.FRIENDS);
        when(settings.findById(1L)).thenReturn(Optional.of(s));
    }

    @Test
    void friendsOnlyProfileIsRestrictedForStrangers() {
        anaShowsProfileOnlyToFriends();
        ana.setBio("Joc în weekend.");
        ana.setCityId(4L);

        var p = service.getPublicProfile(2L, 1L);

        assertTrue(p.restricted());
        assertEquals("ana", p.username());
        assertNull(p.description());
        assertNull(p.cityId());
        assertTrue(p.sports().isEmpty());
    }

    @Test
    void friendsOnlyProfileIsFullForFriends() {
        anaShowsProfileOnlyToFriends();
        ana.setBio("Joc în weekend.");
        friends = true;

        var p = service.getPublicProfile(2L, 1L);

        assertFalse(p.restricted());
        assertEquals("Joc în weekend.", p.description());
    }

    @Test
    void ownProfileIsNeverRestricted() {
        anaShowsProfileOnlyToFriends();
        assertFalse(service.getPublicProfile(1L, 1L).restricted());
    }

    @Test
    void similarUsersShareASportAndSameCityComesFirst() {
        ana.setCityId(4L);
        when(userSports.findByUserId(1L)).thenReturn(List.of(new UserSport(1L, 1L, SportLevel.BEGINNER)));
        when(userSports.findBySportIdIn(List.of(1L))).thenReturn(List.of(
                new UserSport(1L, 1L, SportLevel.BEGINNER),
                new UserSport(2L, 1L, SportLevel.BEGINNER),
                new UserSport(3L, 1L, SportLevel.ADVANCED),
                new UserSport(5L, 1L, SportLevel.ADVANCED)));
        User other = user(2L, "bogdan", "b@demo.ro");
        other.setCityId(9L);
        User sameCity = user(3L, "ioana", "i@demo.ro");
        sameCity.setCityId(4L);
        User deleted = user(5L, "sters-5", "s@demo.ro");
        deleted.setActive(false);
        when(users.findAllById(any())).thenReturn(List.of(other, sameCity, deleted));

        assertEquals(List.of(3L, 2L), service.findSimilarUsers(1L));
    }
}
