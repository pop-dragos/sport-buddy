package ro.utcn.sportbuddy.users.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.utcn.sportbuddy.catalog.service.CatalogService;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.users.dto.*;
import ro.utcn.sportbuddy.users.model.*;
import ro.utcn.sportbuddy.users.repository.UserRepository;
import ro.utcn.sportbuddy.users.repository.UserSettingsRepository;
import ro.utcn.sportbuddy.users.repository.UserSportRepository;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class UserService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");

    private final UserRepository userRepository;
    private final UserSportRepository userSportRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final CatalogService catalogService;

    public UserService(UserRepository userRepository, UserSportRepository userSportRepository,
                       UserSettingsRepository userSettingsRepository, PasswordEncoder passwordEncoder,
                       CatalogService catalogService) {
        this.userRepository = userRepository;
        this.userSportRepository = userSportRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.passwordEncoder = passwordEncoder;
        this.catalogService = catalogService;
    }

    // ---------- citire ----------

    public List<UserSummary> findAll() {
        return userRepository.findAll().stream().map(UserSummary::from).toList();
    }

    public UserSummary getSummary(Long id) {
        return UserSummary.from(getEntity(id));
    }

    public Optional<Long> findIdByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username).map(User::getId);
    }

    public Optional<Long> findIdByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(User::getId);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public boolean isActive(Long id) {
        return userRepository.findById(id).map(User::isActive).orElse(false);
    }

    public Optional<UserCredentials> findCredentials(String identifier) {
        return userRepository.findByUsernameIgnoreCase(identifier)
                .or(() -> userRepository.findByEmailIgnoreCase(identifier))
                .filter(User::isActive)
                .map(u -> new UserCredentials(u.getId(), u.getPasswordHash()));
    }

    @Transactional(readOnly = true)
    public UserMe getMe(Long id) {
        return toMe(getEntity(id));
    }

    // ---------- creare ----------

    @Transactional
    public Long createUser(String fullName, String email, String passwordHash, Integer age,
                           String phone, Long cityId, String bio, AuthProvider provider,
                           List<SportLevelDto> sports) {
        Map<Long, SportLevel> chosenSports = validatedSports(sports);
        User u = new User();
        u.setUsername(generateUsername(fullName, email));
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setAuthProvider(provider);
        u.setAge(age);
        u.setPhone(phone);
        u.setCityId(cityId);
        u.setBio(bio);
        userRepository.save(u);
        chosenSports.forEach((sportId, level) ->
                userSportRepository.save(new UserSport(u.getId(), sportId, level)));
        userSettingsRepository.save(new UserSettings(u.getId()));
        return u.getId();
    }

    // ---------- profil ----------

    // Se schimbă doar câmpurile trimise. Telefonul și descrierea se șterg trimițându-le goale.
    @Transactional
    public UserMe updateMe(Long id, UpdateProfileRequest req) {
        User u = getEntity(id);
        if (req.fullName() != null) {
            if (req.fullName().isBlank()) throw invalid("fullName", "Numele nu poate fi gol.");
            u.setFullName(req.fullName().trim());
        }
        if (req.email() != null) {
            String email = req.email().trim();
            if (email.isEmpty()) throw invalid("email", "Emailul nu poate fi gol.");
            userRepository.findByEmailIgnoreCase(email)
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ApiException(HttpStatus.CONFLICT, "EMAIL_TAKEN",
                                "Există deja un cont cu acest email.");
                    });
            u.setEmail(email);
        }
        if (req.phone() != null) u.setPhone(blankToNull(req.phone()));
        if (req.age() != null) u.setAge(req.age());
        if (req.cityId() != null) {
            if (catalogService.findCity(req.cityId()).isEmpty()) {
                throw invalid("cityId", "Orașul ales nu există.");
            }
            u.setCityId(req.cityId());
        }
        if (req.description() != null) u.setBio(blankToNull(req.description()));
        return toMe(u);
    }

    // Lista trimisă înlocuiește sporturile: cele lipsă se scot, cele rămase își schimbă nivelul.
    @Transactional
    public UserMe updateSports(Long id, List<SportLevelDto> sports) {
        User u = getEntity(id);
        Map<Long, SportLevel> wanted = validatedSports(sports);
        for (UserSport existing : userSportRepository.findByUserId(id)) {
            SportLevel level = wanted.remove(existing.getSportId());
            if (level == null) userSportRepository.delete(existing);
            else existing.setDeclaredLevel(level);
        }
        wanted.forEach((sportId, level) -> userSportRepository.save(new UserSport(id, sportId, level)));
        return toMe(u);
    }

    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User u = getEntity(id);
        if (u.getPasswordHash() == null || !passwordEncoder.matches(currentPassword, u.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "WRONG_PASSWORD", "Parola curentă este greșită.");
        }
        u.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    // Contul devine inactiv și anonimizat. Rândul rămâne, ca lobby-urile și review-urile să nu se strice.
    @Transactional
    public void deleteMe(Long id) {
        User u = getEntity(id);
        u.setActive(false);
        u.setUsername("sters-" + id);
        u.setFullName("Utilizator șters");
        u.setEmail("sters-" + id + "@sters.invalid");
        u.setPasswordHash(null);
        u.setPhone(null);
        u.setPhotoPath(null);
        u.setBio(null);
        u.setCityId(null);
        userSportRepository.deleteByUserId(id);
        userSettingsRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PublicProfile getPublicProfile(Long id) {
        // Respectarea setării profileVisibility = FRIENDS vine odată cu modulul friends.
        User u = userRepository.findById(id).filter(User::isActive).orElseThrow(this::notFound);
        return new PublicProfile(u.getId(), u.getUsername(), u.getFullName(), avatarUrl(u), u.getCityId(),
                u.getBio(), u.getCreatedAt().atZone(ZONE).toLocalDate(), sportsOf(u.getId()));
    }

    // ---------- setări ----------

    @Transactional
    public UserSettingsDto getSettings(Long userId) {
        return toDto(settingsOf(userId));
    }

    @Transactional
    public UserSettingsDto updateSettings(Long userId, UserSettingsDto dto) {
        UserSettings s = settingsOf(userId);
        s.setTheme(dto.theme());
        s.setShowWeather(dto.showWeather());
        s.setNotifJoinRequests(dto.notifyJoinRequests());
        s.setNotifMessages(dto.notifyMessages());
        s.setNotifReminders(dto.notifyReminder());
        s.setNotifEmailDigest(dto.notifyWeeklyDigest());
        s.setProfileVisibility(dto.profileVisibility());
        s.setWhoCanAddMe(dto.whoCanAddMe());
        s.setShowFriendList(dto.showFriendsList());
        return toDto(s);
    }

    // ---------- helperi ----------

    private User getEntity(Long id) {
        return userRepository.findById(id).orElseThrow(this::notFound);
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Utilizatorul nu există.");
    }

    private static ApiException invalid(String field, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.",
                Map.of(field, message));
    }

    private static String blankToNull(String s) {
        return s.isBlank() ? null : s.trim();
    }

    // Cel puțin un sport, fără dubluri, toate existente în catalog.
    private Map<Long, SportLevel> validatedSports(List<SportLevelDto> sports) {
        if (sports == null || sports.isEmpty()) {
            throw invalid("sports", "Alege cel puțin un sport.");
        }
        Map<Long, SportLevel> unique = new LinkedHashMap<>();
        for (SportLevelDto s : sports) {
            if (s == null || s.sportId() == null || s.level() == null) {
                throw invalid("sports", "Sporturile selectate sunt invalide.");
            }
            unique.put(s.sportId(), s.level());
        }
        for (Long sportId : unique.keySet()) {
            if (catalogService.findSport(sportId).isEmpty()) {
                throw invalid("sports", "Sportul ales nu există.");
            }
        }
        return unique;
    }

    private UserSettings settingsOf(Long userId) {
        return userSettingsRepository.findById(userId)
                .orElseGet(() -> userSettingsRepository.save(new UserSettings(getEntity(userId).getId())));
    }

    private static UserSettingsDto toDto(UserSettings s) {
        return new UserSettingsDto(s.getTheme(), s.isShowWeather(), s.isNotifJoinRequests(),
                s.isNotifMessages(), s.isNotifReminders(), s.isNotifEmailDigest(),
                s.getProfileVisibility(), s.getWhoCanAddMe(), s.isShowFriendList());
    }

    private List<SportLevelDto> sportsOf(Long userId) {
        return userSportRepository.findByUserId(userId).stream()
                .map(s -> new SportLevelDto(s.getSportId(), s.getDeclaredLevel())).toList();
    }

    public UserMe toMe(User u) {
        List<SportLevelDto> sports = sportsOf(u.getId());
        return new UserMe(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getAge(), avatarUrl(u), u.getCityId(), u.getBio(),
                LocalDateTime.ofInstant(u.getCreatedAt(), ZONE), sports, completion(u, sports));
    }

    private static String avatarUrl(User u) {
        return u.getPhotoPath() == null ? null : "/api/uploads/" + u.getPhotoPath();
    }

    private static boolean has(String s) {
        return s != null && !s.isBlank();
    }

    // 4 câmpuri obligatorii x 10% + 4 opționale x 15% = 100%
    private static ProfileCompletion completion(User u, List<SportLevelDto> sports) {
        int percent = 0;
        if (has(u.getFullName())) percent += 10;
        if (has(u.getEmail())) percent += 10;
        if (u.getAge() != null) percent += 10;
        if (!sports.isEmpty()) percent += 10;

        List<String> missing = new ArrayList<>();
        if (has(u.getPhone())) percent += 15; else missing.add("phone");
        if (has(u.getPhotoPath())) percent += 15; else missing.add("avatar");
        if (u.getCityId() != null) percent += 15; else missing.add("city");
        if (has(u.getBio())) percent += 15; else missing.add("description");
        return new ProfileCompletion(percent, missing);
    }

    private String generateUsername(String fullName, String email) {
        String base = slug(fullName);
        if (base.isEmpty()) base = slug(email.substring(0, email.indexOf('@')));
        if (base.isEmpty()) base = "user";
        String candidate = base;
        int n = 1;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            n++;
            candidate = base + n;
        }
        return candidate;
    }

    private static String slug(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase();
        return n.replaceAll("[^a-z0-9]+", ".").replaceAll("^\\.+|\\.+$", "");
    }
}