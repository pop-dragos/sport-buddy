package ro.utcn.sportbuddy.users.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.users.dto.*;
import ro.utcn.sportbuddy.users.model.*;
import ro.utcn.sportbuddy.users.repository.UserRepository;
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

    public UserService(UserRepository userRepository, UserSportRepository userSportRepository) {
        this.userRepository = userRepository;
        this.userSportRepository = userSportRepository;
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
        return userRepository.existsById(id);
    }

    public Optional<UserCredentials> findCredentials(String identifier) {
        return userRepository.findByUsernameIgnoreCase(identifier)
                .or(() -> userRepository.findByEmailIgnoreCase(identifier))
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
        saveSports(u.getId(), sports);
        return u.getId();
    }

    // ---------- helperi ----------

    private User getEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Utilizatorul nu există."));
    }

    private void saveSports(Long userId, List<SportLevelDto> sports) {
        Map<Long, SportLevel> unique = new LinkedHashMap<>();
        for (SportLevelDto s : sports) {
            if (s == null || s.sportId() == null || s.level() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                        "Sporturile selectate sunt invalide.");
            }
            unique.put(s.sportId(), s.level());
        }
        unique.forEach((sportId, level) ->
                userSportRepository.save(new UserSport(userId, sportId, level)));
    }

    public UserMe toMe(User u) {
        List<SportLevelDto> sports = userSportRepository.findByUserId(u.getId()).stream()
                .map(s -> new SportLevelDto(s.getSportId(), s.getDeclaredLevel())).toList();
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