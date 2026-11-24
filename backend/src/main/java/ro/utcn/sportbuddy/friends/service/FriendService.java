package ro.utcn.sportbuddy.friends.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.catalog.dto.CityDto;
import ro.utcn.sportbuddy.catalog.dto.SportDto;
import ro.utcn.sportbuddy.catalog.service.CatalogService;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.friends.dto.*;
import ro.utcn.sportbuddy.friends.model.DirectMessage;
import ro.utcn.sportbuddy.friends.model.Friendship;
import ro.utcn.sportbuddy.friends.model.FriendshipStatus;
import ro.utcn.sportbuddy.friends.repository.DirectMessageRepository;
import ro.utcn.sportbuddy.friends.repository.FriendshipRepository;
import ro.utcn.sportbuddy.lobbies.service.LobbyService;
import ro.utcn.sportbuddy.ratings.service.RatingService;
import ro.utcn.sportbuddy.users.dto.PublicProfile;
import ro.utcn.sportbuddy.users.dto.SportLevelDto;
import ro.utcn.sportbuddy.users.dto.UserSettingsDto;
import ro.utcn.sportbuddy.users.model.ProfileVisibility;
import ro.utcn.sportbuddy.users.model.WhoCanAddMe;
import ro.utcn.sportbuddy.users.service.FriendshipChecker;
import ro.utcn.sportbuddy.users.service.UserService;

import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class FriendService implements FriendshipChecker {

    public static final int MAX_SUGGESTIONS = 10;
    public static final int MESSAGE_HISTORY = 100;
    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");

    private final FriendshipRepository friendshipRepository;
    private final DirectMessageRepository messageRepository;
    private final UserService userService;
    private final CatalogService catalogService;
    private final RatingService ratingService;
    private final LobbyService lobbyService;

    public FriendService(FriendshipRepository friendshipRepository, DirectMessageRepository messageRepository,
                         UserService userService, CatalogService catalogService, RatingService ratingService,
                         LobbyService lobbyService) {
        this.friendshipRepository = friendshipRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.catalogService = catalogService;
        this.ratingService = ratingService;
        this.lobbyService = lobbyService;
    }

    // ---------- starea prieteniei ----------

    @Override
    @Transactional(readOnly = true)
    public boolean areFriends(Long a, Long b) {
        return friendshipRepository.findBetween(a, b).stream().anyMatch(Friendship::isAccepted);
    }

    @Transactional(readOnly = true)
    public FriendStatus status(Long me, Long other) {
        for (Friendship f : friendshipRepository.findBetween(me, other)) {
            if (f.isAccepted()) return new FriendStatus(FriendStatus.Status.FRIENDS, null);
            if (f.isPending()) {
                return new FriendStatus(f.getRequesterId().equals(me)
                        ? FriendStatus.Status.PENDING_SENT : FriendStatus.Status.PENDING_RECEIVED, f.getId());
            }
        }
        return FriendStatus.none();
    }

    // Starea față de mai mulți utilizatori deodată (de exemplu, participanții unui lobby), cu o singură
    // interogare pentru relațiile mele. Pentru NONE spune și dacă pot trimite cerere (canAdd).
    // Nu e readOnly: whoCanAddMe poate crea setările implicite ale unui utilizator vechi.
    public static final int MAX_STATUSES = 100;

    @Transactional
    public Map<Long, FriendStatus> statuses(Long me, Collection<Long> ids) {
        List<Long> others = ids.stream().filter(id -> id != null && !id.equals(me)).distinct().toList();
        if (others.size() > MAX_STATUSES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                    "Cel mult " + MAX_STATUSES + " de utilizatori odată.");
        }
        Map<Long, Friendship> relation = new HashMap<>();
        Set<Long> myFriends = new HashSet<>();
        for (Friendship f : friendshipRepository.findInvolving(me)) {
            Long other = f.getRequesterId().equals(me) ? f.getAddresseeId() : f.getRequesterId();
            if (f.isAccepted()) myFriends.add(other);
            if (f.isAccepted() || f.isPending()) relation.merge(other, f, (a, b) -> a.isAccepted() ? a : b);
        }

        Map<Long, FriendStatus> result = new LinkedHashMap<>();
        for (Long other : others) {
            Friendship f = relation.get(other);
            if (f != null && f.isAccepted()) {
                result.put(other, new FriendStatus(FriendStatus.Status.FRIENDS, null));
            } else if (f != null) {
                result.put(other, new FriendStatus(f.getRequesterId().equals(me)
                        ? FriendStatus.Status.PENDING_SENT : FriendStatus.Status.PENDING_RECEIVED, f.getId()));
            } else {
                result.put(other, new FriendStatus(FriendStatus.Status.NONE, null, canAdd(other, myFriends)));
            }
        }
        return result;
    }

    // Aceleași reguli ca la trimiterea cererii (checkCanAdd), fără excepții.
    private boolean canAdd(Long target, Set<Long> myFriends) {
        if (!userService.isActive(target)) return false;
        WhoCanAddMe rule = userService.whoCanAddMe(target);
        if (rule == WhoCanAddMe.NOBODY) return false;
        return rule != WhoCanAddMe.FRIENDS_OF_FRIENDS || !Collections.disjoint(myFriends, friendIds(target));
    }

    // ---------- cereri ----------

    // Dacă celălalt mi-a trimis deja o cerere, a mea o acceptă direct: amândoi vor același lucru.
    @Transactional
    public FriendStatus sendRequest(Long me, Long target) {
        if (me.equals(target)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.",
                    Map.of("userId", "Nu îți poți trimite cerere ție."));
        }
        if (!userService.isActive(target)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Utilizatorul nu există.");
        }

        for (Friendship f : friendshipRepository.findBetween(me, target)) {
            if (f.isAccepted()) {
                throw new ApiException(HttpStatus.CONFLICT, "ALREADY_FRIENDS", "Sunteți deja prieteni.");
            }
            if (f.isPending() && f.getRequesterId().equals(me)) {
                throw new ApiException(HttpStatus.CONFLICT, "REQUEST_EXISTS", "Ai trimis deja o cerere.");
            }
            if (f.isPending()) {
                f.accept();
                return new FriendStatus(FriendStatus.Status.FRIENDS, null);
            }
            // o cerere refuzată mai demult nu blochează una nouă; flush ca rândul vechi să dispară înainte de inserare
            friendshipRepository.delete(f);
            friendshipRepository.flush();
        }

        checkCanAdd(me, target);
        Friendship created = friendshipRepository.save(new Friendship(me, target));
        return new FriendStatus(FriendStatus.Status.PENDING_SENT, created.getId());
    }

    private void checkCanAdd(Long me, Long target) {
        WhoCanAddMe rule = userService.whoCanAddMe(target);
        if (rule == WhoCanAddMe.NOBODY) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FRIEND_REQUESTS_DISABLED",
                    "Acest utilizator nu primește cereri de prietenie.");
        }
        if (rule == WhoCanAddMe.FRIENDS_OF_FRIENDS && Collections.disjoint(friendIds(me), friendIds(target))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FRIEND_REQUESTS_DISABLED",
                    "Acest utilizator primește cereri doar de la prietenii prietenilor lui.");
        }
    }

    @Transactional
    public void accept(Long me, Long requestId) {
        received(me, requestId).accept();
    }

    @Transactional
    public void reject(Long me, Long requestId) {
        received(me, requestId).decline();
    }

    // Doar cel care a trimis cererea o poate retrage, cât timp e în așteptare.
    @Transactional
    public void cancel(Long me, Long requestId) {
        Friendship f = friendshipRepository.findById(requestId)
                .filter(r -> r.getRequesterId().equals(me))
                .orElseThrow(FriendService::requestNotFound);
        requirePending(f);
        friendshipRepository.delete(f);
    }

    @Transactional
    public void removeFriend(Long me, Long other) {
        Friendship f = friendshipRepository.findBetween(me, other).stream()
                .filter(Friendship::isAccepted).findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Nu sunteți prieteni."));
        friendshipRepository.delete(f);
    }

    // Cererea trebuie să-mi fie adresată mie; altfel răspund ca și cum n-ar exista.
    private Friendship received(Long me, Long requestId) {
        Friendship f = friendshipRepository.findById(requestId)
                .filter(r -> r.getAddresseeId().equals(me))
                .orElseThrow(FriendService::requestNotFound);
        requirePending(f);
        return f;
    }

    private static void requirePending(Friendship f) {
        if (!f.isPending()) {
            throw new ApiException(HttpStatus.CONFLICT, "REQUEST_NOT_PENDING", "Cererea nu mai este în așteptare.");
        }
    }

    private static ApiException requestNotFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Cererea nu există.");
    }

    // ---------- liste ----------

    @Transactional(readOnly = true)
    public List<FriendItem> listFriends(Long me, String query) {
        String q = normalize(query);
        return toItems(me, friendIds(me)).stream()
                .filter(f -> q.isEmpty() || normalize(f.username()).contains(q) || normalize(f.fullName()).contains(q))
                .sorted(Comparator.comparing(f -> normalize(f.fullName())))
                .toList();
    }

    // Prietenii altcuiva, pentru profilul lui. Respectă setările lui: „Lista de prieteni vizibilă” și,
    // la un profil vizibil doar prietenilor, faptul că îi sunt prieten. Nu e readOnly: getSettings poate
    // crea setările implicite ale unui utilizator vechi.
    @Transactional
    public List<FriendItem> friendsOf(Long viewer, Long target) {
        if (!userService.isActive(target)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Utilizatorul nu există.");
        }
        if (!viewer.equals(target)) {
            UserSettingsDto settings = userService.getSettings(target);
            boolean visible = settings.showFriendsList()
                    && (settings.profileVisibility() == ProfileVisibility.EVERYONE || areFriends(viewer, target));
            if (!visible) {
                throw new ApiException(HttpStatus.FORBIDDEN, "FRIEND_LIST_HIDDEN",
                        "Lista de prieteni a acestui utilizator nu este vizibilă.");
            }
        }
        return toItems(viewer, friendIds(target)).stream()
                .sorted(Comparator.comparing(f -> normalize(f.fullName())))
                .toList();
    }

    @Transactional(readOnly = true)
    public FriendRequests requests(Long me) {
        List<Friendship> incoming = friendshipRepository
                .findByAddresseeIdAndStatusOrderByCreatedAtDesc(me, FriendshipStatus.PENDING);
        List<Friendship> outgoing = friendshipRepository
                .findByRequesterIdAndStatusOrderByCreatedAtDesc(me, FriendshipStatus.PENDING);
        return new FriendRequests(toRequestItems(me, incoming), toRequestItems(me, outgoing));
    }

    // „Posibili cunoscuți”: întâi oamenii cu care am jucat în lobby-uri (cei cu mai multe meciuri împreună
    // primii), apoi, până la MAX_SUGGESTIONS, jucători cu un sport comun, ca lista să nu fie goală la conturile noi.
    // Fără prieteni și fără cererile în așteptare; o cerere refuzată nu scoate pe nimeni din sugestii.
    @Transactional(readOnly = true)
    public List<FriendItem> suggestions(Long me) {
        Set<Long> excluded = new HashSet<>();
        for (Friendship f : friendshipRepository.findInvolving(me)) {
            if (f.getStatus() != FriendshipStatus.DECLINED) excluded.add(f.otherThan(me));
        }

        List<Long> coPlayers = lobbyService.coPlayerIds(me).stream()
                .filter(id -> !excluded.contains(id))
                .sorted()
                .toList();
        List<FriendItem> result = new ArrayList<>(toItems(me, coPlayers));
        result.sort(Comparator.comparing((FriendItem f) -> f.matchesTogether() == null ? 0 : f.matchesTogether())
                .reversed());
        if (result.size() >= MAX_SUGGESTIONS) return result.subList(0, MAX_SUGGESTIONS);

        Set<Long> taken = new HashSet<>(coPlayers);
        List<Long> similar = userService.findSimilarUsers(me).stream()
                .filter(id -> !excluded.contains(id) && !taken.contains(id))
                .limit(MAX_SUGGESTIONS - result.size())
                .toList();
        result.addAll(toItems(me, similar));
        return result;
    }

    // ---------- mesaje directe ----------

    // Fără afterId: ultimele mesaje. Cu afterId: doar cele noi (frontend-ul întreabă la 3 secunde).
    @Transactional
    public List<DirectMessageDto> messages(Long me, Long other, Long afterId) {
        requireFriends(me, other);
        List<DirectMessage> found;
        if (afterId == null) {
            found = new ArrayList<>(messageRepository.findLatest(me, other, PageRequest.of(0, MESSAGE_HISTORY)));
            Collections.reverse(found);
        } else {
            found = messageRepository.findAfter(me, other, afterId);
        }
        messageRepository.markRead(other, me, Instant.now());
        return found.stream().map(FriendService::toDto).toList();
    }

    @Transactional
    public DirectMessageDto sendMessage(Long me, Long other, String text) {
        requireFriends(me, other);
        if (text == null || text.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.",
                    Map.of("message", "Scrie un mesaj."));
        }
        return toDto(messageRepository.save(new DirectMessage(me, other, text.trim())));
    }

    private void requireFriends(Long me, Long other) {
        if (!areFriends(me, other)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_FRIENDS", "Poți trimite mesaje doar prietenilor.");
        }
    }

    // ---------- helperi ----------

    private Set<Long> friendIds(Long userId) {
        Set<Long> ids = new LinkedHashSet<>();
        for (Friendship f : friendshipRepository.findAccepted(userId)) ids.add(f.otherThan(userId));
        return ids;
    }

    private List<FriendRequestItem> toRequestItems(Long me, List<Friendship> requests) {
        Map<Long, FriendItem> users = new HashMap<>();
        for (FriendItem item : toItems(me, requests.stream().map(f -> f.otherThan(me)).toList())) {
            users.put(item.userId(), item);
        }
        return requests.stream()
                .filter(f -> users.containsKey(f.otherThan(me)))
                .map(f -> new FriendRequestItem(f.getId(), users.get(f.otherThan(me)), local(f.getCreatedAt())))
                .toList();
    }

    // Conturile șterse dispar din liste (findProfiles întoarce doar utilizatori activi). Ordinea e cea din userIds.
    // matchesTogether: meciurile încheiate jucate de mine împreună cu fiecare (din lobbies).
    private List<FriendItem> toItems(Long me, Collection<Long> userIds) {
        if (userIds.isEmpty()) return List.of();
        Map<Long, String> sportCodes = new HashMap<>();
        for (SportDto s : catalogService.getSports()) sportCodes.put(s.id(), s.code());
        Map<Long, String> cityNames = new HashMap<>();
        Map<Long, PublicProfile> profiles = userService.findProfiles(userIds);

        List<FriendItem> items = new ArrayList<>();
        for (Long id : userIds) {
            PublicProfile p = profiles.get(id);
            if (p == null) continue;
            String city = p.cityId() == null ? null : cityNames.computeIfAbsent(p.cityId(),
                    cityId -> catalogService.findCity(cityId).map(CityDto::name).orElse(null));
            List<String> sports = p.sports().stream().map(SportLevelDto::sportId)
                    .map(sportCodes::get).filter(Objects::nonNull).toList();
            items.add(new FriendItem(p.id(), p.username(), p.fullName(), p.avatarUrl(), city, sports,
                    lobbyService.stats(p.id(), me).matchesTogether(), ratingService.getStats(p.id()).average()));
        }
        return items;
    }

    private static DirectMessageDto toDto(DirectMessage m) {
        return new DirectMessageDto(m.getId(), m.getSenderId(), m.getText(), local(m.getSentAt()));
    }

    private static LocalDateTime local(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZONE);
    }

    // căutare fără diacritice și fără majuscule: „stefan” găsește „Ștefan”
    private static String normalize(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase().trim();
    }
}
