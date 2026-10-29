package ro.utcn.sportbuddy.lobbies.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.catalog.dto.CityDto;
import ro.utcn.sportbuddy.catalog.dto.FieldDto;
import ro.utcn.sportbuddy.catalog.dto.SportDto;
import ro.utcn.sportbuddy.catalog.service.CatalogService;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.common.events.AttendanceSavedEvent;
import ro.utcn.sportbuddy.common.events.LobbyCancelledEvent;
import ro.utcn.sportbuddy.common.events.LobbyLockedEvent;
import ro.utcn.sportbuddy.common.events.PlayerCancelledEvent;
import ro.utcn.sportbuddy.common.events.PlayerJoinedEvent;
import ro.utcn.sportbuddy.lobbies.dto.AttendanceRequest;
import ro.utcn.sportbuddy.lobbies.dto.CalendarDays;
import ro.utcn.sportbuddy.lobbies.dto.CreateLobbyRequest;
import ro.utcn.sportbuddy.lobbies.dto.JoinRequestResult;
import ro.utcn.sportbuddy.lobbies.dto.LeaveResult;
import ro.utcn.sportbuddy.lobbies.dto.LobbyDetails;
import ro.utcn.sportbuddy.lobbies.dto.LobbyStats;
import ro.utcn.sportbuddy.lobbies.dto.LobbySummary;
import ro.utcn.sportbuddy.lobbies.dto.PaymentTarget;
import ro.utcn.sportbuddy.lobbies.dto.TodoCounts;
import ro.utcn.sportbuddy.lobbies.model.JoinRequest;
import ro.utcn.sportbuddy.lobbies.model.JoinRequestStatus;
import ro.utcn.sportbuddy.lobbies.model.Lobby;
import ro.utcn.sportbuddy.lobbies.model.LobbyPlayer;
import ro.utcn.sportbuddy.lobbies.model.LobbyStatus;
import ro.utcn.sportbuddy.lobbies.model.PaymentMethod;
import ro.utcn.sportbuddy.lobbies.model.PlayerStatus;
import ro.utcn.sportbuddy.lobbies.repository.JoinRequestRepository;
import ro.utcn.sportbuddy.lobbies.repository.LobbyPlayerRepository;
import ro.utcn.sportbuddy.lobbies.repository.LobbyRepository;
import ro.utcn.sportbuddy.ratings.dto.RatingStatsDto;
import ro.utcn.sportbuddy.ratings.service.RatingService;
import ro.utcn.sportbuddy.users.dto.UserSummary;
import ro.utcn.sportbuddy.users.service.UserService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class LobbyService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");
    // cât considerăm că durează un meci, ca să îl marcăm încheiat
    private static final Duration MATCH_DURATION = Duration.ofHours(2);
    // jucătorii care ocupă un loc: încă în lobby sau, după meci, bifați prezenți ori absenți
    private static final List<PlayerStatus> ACTIVE =
            List.of(PlayerStatus.JOINED, PlayerStatus.ATTENDED, PlayerStatus.NO_SHOW);
    // cei care au jucat sau joacă efectiv (fără absenți), pentru profil și „jucate împreună"
    private static final List<PlayerStatus> PLAYED = List.of(PlayerStatus.JOINED, PlayerStatus.ATTENDED);

    private final LobbyRepository lobbyRepository;
    private final LobbyPlayerRepository lobbyPlayerRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final CatalogService catalogService;
    private final UserService userService;
    private final RatingService ratingService;
    private final LockDeadlineCalculator deadlineCalculator;
    private final PriceCalculator priceCalculator;
    private final ApplicationEventPublisher events;

    public LobbyService(LobbyRepository lobbyRepository, LobbyPlayerRepository lobbyPlayerRepository,
            JoinRequestRepository joinRequestRepository, CatalogService catalogService, UserService userService,
            RatingService ratingService, LockDeadlineCalculator deadlineCalculator, PriceCalculator priceCalculator,
            ApplicationEventPublisher events) {
        this.events = events;
        this.lobbyRepository = lobbyRepository;
        this.lobbyPlayerRepository = lobbyPlayerRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.catalogService = catalogService;
        this.userService = userService;
        this.ratingService = ratingService;
        this.deadlineCalculator = deadlineCalculator;
        this.priceCalculator = priceCalculator;
    }

    // ---------- creare ----------

    @Transactional
    public LobbyDetails create(Long userId, CreateLobbyRequest req) {
        validatePlayers(req);

        FieldDto field = catalogService.findField(req.fieldId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.",
                        Map.of("fieldId", "Terenul nu există.")));
        if (!field.sportId().equals(req.sportId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "SPORT_FIELD_MISMATCH",
                    "Terenul ales nu este pentru acest sport.");
        }

        LocalDateTime now = LocalDateTime.now(ZONE);
        if (!deadlineCalculator.isValidStart(now, req.dateTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_DATE_TIME",
                    "Meciul trebuie să fie cu cel puțin 3 ore în viitor, iar minutele 00, 15, 30 sau 45.");
        }

        Lobby lobby = new Lobby();
        lobby.setCreatorId(userId);
        lobby.setSportId(req.sportId());
        lobby.setCityId(field.cityId());
        lobby.setFieldId(field.id());
        lobby.setStartAt(req.dateTime());
        lobby.setCapacity(req.maxPlayers());
        lobby.setMinPlayers(req.minPlayers());
        lobby.setFieldCost(BigDecimal.valueOf(req.fieldCost()));
        // organizatorul are cont și intră ca LobbyPlayer; ceilalți jucători existenți sunt „aduși"
        lobby.setGuestCount(req.alreadyPlaying() - 1);
        lobby.setLockDeadline(deadlineCalculator.deadline(now, req.dateTime()));
        lobby = lobbyRepository.save(lobby);

        LobbyPlayer host = new LobbyPlayer();
        host.setLobbyId(lobby.getId());
        host.setUserId(userId);
        lobbyPlayerRepository.save(host);

        return toDetails(lobby, userId);
    }

    private void validatePlayers(CreateLobbyRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (req.minPlayers() > req.maxPlayers()) {
            fields.put("minPlayers", "Minimul nu poate depăși numărul de locuri.");
        }
        if (req.alreadyPlaying() > req.maxPlayers()) {
            fields.put("alreadyPlaying", "Jucătorii existenți nu pot depăși numărul de locuri.");
        }
        if (!fields.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.", fields);
        }
    }

    // ---------- cereri de alăturare ----------

    @Transactional
    public JoinRequestResult requestToJoin(Long lobbyId, Long userId) {
        Lobby lobby = getLobby(lobbyId);
        if (isParticipant(lobbyId, userId)) {
            throw new ApiException(HttpStatus.CONFLICT, "ALREADY_IN_LOBBY", "Ești deja în acest lobby.");
        }
        requireOpen(lobby);
        if (LocalDateTime.now(ZONE).isAfter(lobby.getLockDeadline())) {
            throw new ApiException(HttpStatus.CONFLICT, "JOIN_CLOSED", "Termenul de înscriere a trecut.");
        }
        requireFreeSlot(lobby);
        if (joinRequestRepository.findByLobbyIdAndUserIdAndStatus(lobbyId, userId, JoinRequestStatus.PENDING).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "ALREADY_REQUESTED", "Ai trimis deja o cerere.");
        }

        JoinRequest request = new JoinRequest();
        request.setLobbyId(lobbyId);
        request.setUserId(userId);
        request = joinRequestRepository.save(request);
        return new JoinRequestResult(request.getId(), request.getStatus().name());
    }

    @Transactional
    public LobbyDetails acceptRequest(Long lobbyId, Long requestId, Long userId) {
        Lobby lobby = getLobby(lobbyId);
        requireHost(lobby, userId);
        JoinRequest request = getPendingRequest(lobbyId, requestId);
        requireOpen(lobby);
        requireFreeSlot(lobby);

        request.setStatus(JoinRequestStatus.ACCEPTED);
        request.setDecidedAt(Instant.now());

        addPlayer(lobbyId, request.getUserId());

        return toDetails(lobby, userId);
    }

    @Transactional
    public LobbyDetails rejectRequest(Long lobbyId, Long requestId, Long userId) {
        Lobby lobby = getLobby(lobbyId);
        requireHost(lobby, userId);
        JoinRequest request = getPendingRequest(lobbyId, requestId);
        request.setStatus(JoinRequestStatus.REJECTED);
        request.setDecidedAt(Instant.now());
        return toDetails(lobby, userId);
    }

    // Ieșirea din lobby. Cine are doar o cerere în așteptare și-o retrage.
    // Cât lobby-ul e OPEN ieșirea e gratuită; după confirmare e anulare târzie.
    @Transactional
    public LeaveResult leave(Long lobbyId, Long userId) {
        Lobby lobby = getLobby(lobbyId);
        if (lobby.getCreatorId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "HOST_CANNOT_LEAVE",
                    "Organizatorul nu poate ieși. Poate anula lobby-ul.");
        }

        LobbyPlayer player = lobbyPlayerRepository.findByLobbyIdAndUserId(lobbyId, userId)
                .filter(p -> p.getStatus() == PlayerStatus.JOINED)
                .orElse(null);
        if (player == null) {
            JoinRequest pending = joinRequestRepository
                    .findByLobbyIdAndUserIdAndStatus(lobbyId, userId, JoinRequestStatus.PENDING)
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "NOT_PARTICIPANT",
                            "Nu ești în acest lobby."));
            pending.setStatus(JoinRequestStatus.WITHDRAWN);
            pending.setDecidedAt(Instant.now());
            return new LeaveResult(false);
        }

        boolean late = switch (lobby.getStatus()) {
            case OPEN -> false;
            case LOCKED -> true;
            default -> throw new ApiException(HttpStatus.CONFLICT, "LOBBY_NOT_OPEN", "Lobby-ul s-a încheiat.");
        };
        player.setStatus(late ? PlayerStatus.CANCELLED_LATE : PlayerStatus.CANCELLED_FREE);
        player.setCancelledAt(Instant.now());
        // ratings (C) dă penalizarea dacă late e true
        events.publishEvent(new PlayerCancelledEvent(lobbyId, userId, late));
        return new LeaveResult(late);
    }

    // ---------- confirmare, anulare, încheiere ----------

    // Organizatorul poate confirma oricând cât lobby-ul e deschis, chiar sub minim.
    @Transactional
    public LobbyDetails lock(Long lobbyId, Long userId) {
        Lobby lobby = getLobby(lobbyId);
        requireHost(lobby, userId);
        requireOpen(lobby);
        lockLobby(lobby);
        return toDetails(lobby, userId);
    }

    // Anularea de către organizator; după confirmare primește penalizare (răspunsul are aceeași formă ca la leave).
    @Transactional
    public LeaveResult cancel(Long lobbyId, Long userId) {
        Lobby lobby = getLobby(lobbyId);
        requireHost(lobby, userId);
        boolean late = switch (lobby.getStatus()) {
            case OPEN -> false;
            case LOCKED -> true;
            default -> throw new ApiException(HttpStatus.CONFLICT, "LOBBY_NOT_OPEN", "Lobby-ul s-a încheiat deja.");
        };
        lobby.setStatus(LobbyStatus.CANCELLED);
        events.publishEvent(new LobbyCancelledEvent(lobbyId, userId, late));
        return new LeaveResult(late);
    }

    // Rulat de LobbyScheduler o dată pe minut.
    @Transactional
    public void processDeadlines() {
        LocalDateTime now = LocalDateTime.now(ZONE);
        lobbyRepository.findByStatusAndLockDeadlineLessThanEqual(LobbyStatus.OPEN, now)
                .forEach(this::processDeadline);
        lobbyRepository.findByStatusAndStartAtLessThanEqual(LobbyStatus.LOCKED, now.minus(MATCH_DURATION))
                .forEach(lobby -> lobby.setStatus(LobbyStatus.FINISHED));
    }

    // La termen: cu minimul atins, lobby-ul se confirmă. Fără minim rămâne deschis
    // (organizatorul vede „minimul nu a fost atins" și poate anula fără penalizare),
    // iar dacă a venit ora meciului și tot nu e confirmat, se anulează.
    private void processDeadline(Lobby lobby) {
        if (occupied(lobby) >= lobby.getMinPlayers()) {
            lockLobby(lobby);
        } else if (!lobby.getStartAt().isAfter(LocalDateTime.now(ZONE))) {
            lobby.setStatus(LobbyStatus.CANCELLED);
            events.publishEvent(new LobbyCancelledEvent(lobby.getId(), lobby.getCreatorId(), false));
        }
    }

    private void lockLobby(Lobby lobby) {
        List<Long> playerIds = lobbyPlayerRepository.findByLobbyIdAndStatus(lobby.getId(), PlayerStatus.JOINED)
                .stream().map(LobbyPlayer::getUserId).toList();
        BigDecimal finalPrice = priceCalculator.pricePerPlayer(lobby.getFieldCost(), occupied(lobby));
        lobby.setFinalPricePerPlayer(finalPrice);
        lobby.setStatus(LobbyStatus.LOCKED);
        lobby.setLockedAt(LocalDateTime.now(ZONE));
        events.publishEvent(new LobbyLockedEvent(lobby.getId(), toLei(finalPrice), playerIds));
    }

    // După meci, organizatorul bifează cine a venit. Se poate face o singură dată.
    // Organizatorul e considerat prezent; cine nu apare în listă e considerat prezent.
    @Transactional
    public LobbyDetails saveAttendance(Long lobbyId, Long userId, AttendanceRequest req) {
        Lobby lobby = getLobby(lobbyId);
        requireHost(lobby, userId);
        if (lobby.getStatus() != LobbyStatus.FINISHED) {
            throw new ApiException(HttpStatus.CONFLICT, "LOBBY_NOT_FINISHED", "Prezența se salvează după meci.");
        }
        if (lobby.isAttendanceSaved()) {
            throw new ApiException(HttpStatus.CONFLICT, "ATTENDANCE_ALREADY_SAVED", "Prezența a fost deja salvată.");
        }

        Map<Long, Boolean> present = new HashMap<>();
        req.attendance().forEach(e -> present.put(e.userId(), e.present()));

        List<LobbyPlayer> players = lobbyPlayerRepository.findByLobbyIdAndStatus(lobbyId, PlayerStatus.JOINED);
        List<Long> presentIds = new ArrayList<>();
        List<Long> absentIds = new ArrayList<>();
        for (LobbyPlayer player : players) {
            boolean isHostPlayer = player.getUserId().equals(lobby.getCreatorId());
            boolean came = isHostPlayer || present.getOrDefault(player.getUserId(), true);
            player.setStatus(came ? PlayerStatus.ATTENDED : PlayerStatus.NO_SHOW);
            // organizatorul apare separat în eveniment (hostUserId), ca în docs/api.md
            if (!isHostPlayer) {
                (came ? presentIds : absentIds).add(player.getUserId());
            }
        }
        lobby.setAttendanceSaved(true);

        // ratings (C): crește prezențele, iar absenții primesc review automat de 1 stea
        events.publishEvent(new AttendanceSavedEvent(lobbyId, lobby.getCreatorId(), presentIds, absentIds));
        return toDetails(lobby, userId);
    }

    // ---------- simulare (doar profilul dev, prin DevLobbyController) ----------

    @Transactional
    public LobbyDetails simulatePassDeadline(Long lobbyId, Long userId) {
        Lobby lobby = getLobby(lobbyId);
        requireHost(lobby, userId);
        requireOpen(lobby);
        lobby.setLockDeadline(LocalDateTime.now(ZONE));
        processDeadline(lobby);
        return toDetails(lobby, userId);
    }

    // Un utilizator demo care nu e încă în lobby intră direct, fără cerere.
    @Transactional
    public LobbyDetails simulateJoin(Long lobbyId, Long userId) {
        Lobby lobby = getLobby(lobbyId);
        requireHost(lobby, userId);
        requireOpen(lobby);
        requireFreeSlot(lobby);
        Long demoId = userService.findAll().stream()
                .map(UserSummary::id)
                .filter(id -> userService.isActive(id) && !isParticipant(lobbyId, id))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "NO_DEMO_PLAYER",
                        "Nu mai există jucători demo care să nu fie în lobby."));
        // dacă avea o cerere în așteptare, o considerăm acceptată
        joinRequestRepository.findByLobbyIdAndUserIdAndStatus(lobbyId, demoId, JoinRequestStatus.PENDING)
                .ifPresent(r -> {
                    r.setStatus(JoinRequestStatus.ACCEPTED);
                    r.setDecidedAt(Instant.now());
                });
        addPlayer(lobbyId, demoId);
        return toDetails(lobby, userId);
    }

    // Jucătorul ales iese din lobby, cu aceleași reguli și același eveniment ca la ieșirea normală.
    @Transactional
    public LobbyDetails simulateCancel(Long lobbyId, Long userId, Long playerId) {
        Lobby lobby = getLobby(lobbyId);
        requireHost(lobby, userId);
        leave(lobbyId, playerId);
        return toDetails(lobby, userId);
    }

    @Transactional
    public LobbyDetails simulateFinish(Long lobbyId, Long userId) {
        Lobby lobby = getLobby(lobbyId);
        requireHost(lobby, userId);
        if (lobby.getStatus() != LobbyStatus.LOCKED) {
            throw new ApiException(HttpStatus.CONFLICT, "LOBBY_NOT_LOCKED", "Confirmă lobby-ul înainte să îl închei.");
        }
        lobby.setStatus(LobbyStatus.FINISHED);
        return toDetails(lobby, userId);
    }

    // ---------- pentru modulul payments ----------

    public PaymentTarget paymentTarget(Long lobbyId, Long userId) {
        Lobby lobby = getLobby(lobbyId);
        LobbyPlayer player = lobbyPlayerRepository.findByLobbyIdAndUserId(lobbyId, userId)
                .filter(p -> ACTIVE.contains(p.getStatus()))
                .orElse(null);
        SportDto sport = catalogService.findSport(lobby.getSportId()).orElseThrow();
        FieldDto field = catalogService.findField(lobby.getFieldId()).orElseThrow();
        return new PaymentTarget(lobbyId, lobby.getStatus().name(), toLei(lobby.getFinalPricePerPlayer()),
                player != null, player != null && player.isPaid(),
                sport.name(), field.name(), lobby.getStartAt());
    }

    // După o plată online reușită: jucătorul apare cu „Online" la participanți.
    @Transactional
    public void markPaidOnline(Long lobbyId, Long userId) {
        LobbyPlayer player = lobbyPlayerRepository.findByLobbyIdAndUserId(lobbyId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "NOT_PARTICIPANT", "Nu ești în acest lobby."));
        player.setPaid(true);
        player.setPaymentMethod(PaymentMethod.ONLINE);
    }

    // Folosit și de chat (C): cine e în lobby, inclusiv după ce meciul s-a încheiat.
    public boolean isParticipant(Long lobbyId, Long userId) {
        return lobbyPlayerRepository.findByLobbyIdAndUserId(lobbyId, userId)
                .map(p -> p.getStatus() == PlayerStatus.JOINED || p.getStatus() == PlayerStatus.ATTENDED)
                .orElse(false);
    }

    // Jucătorul intră în lobby. Cine a ieșit și revine are deja un rând în lobby_players
    // (unic pe lobby + utilizator), așa că îl refolosim.
    private void addPlayer(Long lobbyId, Long userId) {
        LobbyPlayer player = lobbyPlayerRepository.findByLobbyIdAndUserId(lobbyId, userId)
                .orElseGet(LobbyPlayer::new);
        player.setLobbyId(lobbyId);
        player.setUserId(userId);
        player.setStatus(PlayerStatus.JOINED);
        player.setCancelledAt(null);
        lobbyPlayerRepository.save(player);
        events.publishEvent(new PlayerJoinedEvent(lobbyId, userId));
    }

    private Lobby getLobby(Long lobbyId) {
        return lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Lobby-ul nu există."));
    }

    private JoinRequest getPendingRequest(Long lobbyId, Long requestId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .filter(r -> r.getLobbyId().equals(lobbyId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Cererea nu există."));
        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "REQUEST_NOT_PENDING", "Cererea a fost deja rezolvată.");
        }
        return request;
    }

    private void requireHost(Lobby lobby, Long userId) {
        if (!lobby.getCreatorId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_HOST", "Doar organizatorul poate face asta.");
        }
    }

    private void requireOpen(Lobby lobby) {
        if (lobby.getStatus() != LobbyStatus.OPEN) {
            throw new ApiException(HttpStatus.CONFLICT, "LOBBY_NOT_OPEN", "Lobby-ul nu mai primește jucători.");
        }
    }

    private void requireFreeSlot(Lobby lobby) {
        if (occupied(lobby) >= lobby.getCapacity()) {
            throw new ApiException(HttpStatus.CONFLICT, "LOBBY_FULL", "Lobby-ul este complet.");
        }
    }

    private int occupied(Lobby lobby) {
        long joined = lobbyPlayerRepository.countByLobbyIdAndStatusIn(lobby.getId(), ACTIVE);
        return priceCalculator.occupiedSlots(lobby.getGuestCount(), joined);
    }

    // ---------- citire ----------

    // Lobby-urile dintr-o zi, pentru lista de sub calendar. Cele anulate nu apar.
    public List<LobbySummary> listForDay(Long cityId, Long sportId, LocalDate date, Long userId) {
        return findVisible(cityId, sportId, date.atStartOfDay(), date.plusDays(1).atStartOfDay()).stream()
                .map(lobby -> toSummary(lobby, userId))
                .toList();
    }

    // Zilele din lună care au cel puțin un lobby; month are forma 2026-10.
    public CalendarDays calendar(Long cityId, Long sportId, String month) {
        YearMonth ym;
        try {
            ym = YearMonth.parse(month);
        } catch (DateTimeParseException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.",
                    Map.of("month", "Luna trebuie să aibă forma 2026-10."));
        }
        List<LocalDate> days = findVisible(cityId, sportId,
                ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay()).stream()
                .map(lobby -> lobby.getStartAt().toLocalDate())
                .distinct()
                .toList();
        return new CalendarDays(days);
    }

    private List<Lobby> findVisible(Long cityId, Long sportId, LocalDateTime from, LocalDateTime to) {
        return lobbyRepository
                .findByCityIdAndSportIdAndStartAtGreaterThanEqualAndStartAtLessThanOrderByStartAtAsc(
                        cityId, sportId, from, to).stream()
                .filter(lobby -> lobby.getStatus() != LobbyStatus.CANCELLED)
                .toList();
    }

    private LobbySummary toSummary(Lobby lobby, Long userId) {
        SportDto sport = catalogService.findSport(lobby.getSportId()).orElseThrow();
        FieldDto field = catalogService.findField(lobby.getFieldId()).orElseThrow();
        CityDto city = catalogService.findCity(lobby.getCityId()).orElseThrow();
        boolean isHost = lobby.getCreatorId().equals(userId);

        List<LobbyPlayer> joined = lobbyPlayerRepository.findByLobbyIdAndStatusIn(lobby.getId(), ACTIVE);
        int occupied = priceCalculator.occupiedSlots(lobby.getGuestCount(), joined.size());
        List<Double> ratings = joined.stream()
                .map(p -> ratingService.getStats(p.getUserId()).average())
                .toList();

        return new LobbySummary(
                lobby.getId(),
                sport.id(), sport.code(), sport.name(),
                field.id(), field.name(),
                city.id(), city.name(),
                lobby.getStartAt(),
                lobby.getStatus().name(),
                lobby.getCapacity(),
                occupied,
                Math.max(0, lobby.getCapacity() - occupied),
                averageRating(ratings),
                myState(lobby, userId),
                isHost,
                isHost ? (int) joinRequestRepository.countByLobbyIdAndStatus(lobby.getId(), JoinRequestStatus.PENDING) : 0);
    }

    // ---------- agenda ----------

    // UPCOMING: meciurile viitoare în care joc; CREATED: create de mine; REQUESTS: cererile mele
    // în așteptare; FINISHED: meciurile încheiate la care am participat.
    public List<LobbySummary> myLobbies(Long userId, String filter) {
        List<Lobby> lobbies = switch (filter.toUpperCase()) {
            case "UPCOMING" -> lobbiesIPlay(userId).stream()
                    .filter(l -> l.getStatus() == LobbyStatus.OPEN || l.getStatus() == LobbyStatus.LOCKED)
                    .sorted(Comparator.comparing(Lobby::getStartAt))
                    .toList();
            case "CREATED" -> lobbyRepository.findByCreatorIdOrderByStartAtDesc(userId);
            case "REQUESTS" -> lobbyRepository.findAllById(
                            joinRequestRepository.findByUserIdAndStatus(userId, JoinRequestStatus.PENDING).stream()
                                    .map(JoinRequest::getLobbyId).toList()).stream()
                    .sorted(Comparator.comparing(Lobby::getStartAt))
                    .toList();
            case "FINISHED" -> lobbiesIPlay(userId).stream()
                    .filter(l -> l.getStatus() == LobbyStatus.FINISHED)
                    .sorted(Comparator.comparing(Lobby::getStartAt).reversed())
                    .toList();
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Date invalide.",
                    Map.of("filter", "Filtrul poate fi UPCOMING, CREATED, REQUESTS sau FINISHED."));
        };
        return lobbies.stream().map(lobby -> toSummary(lobby, userId)).toList();
    }

    public TodoCounts todo(Long userId) {
        List<Lobby> created = lobbyRepository.findByCreatorIdOrderByStartAtDesc(userId);
        int pendingRequests = created.stream()
                .filter(l -> l.getStatus() == LobbyStatus.OPEN)
                .mapToInt(l -> (int) joinRequestRepository.countByLobbyIdAndStatus(l.getId(), JoinRequestStatus.PENDING))
                .sum();
        int attendanceToSave = (int) created.stream()
                .filter(l -> l.getStatus() == LobbyStatus.FINISHED && !l.isAttendanceSaved())
                .count();
        // ca jucător: meciuri încheiate la care am venit și n-am notat colegii,
        // și meciuri confirmate cu preț, neplătite încă online
        List<LobbyPlayer> myPlayers = lobbyPlayerRepository.findByUserIdAndStatusIn(userId, ACTIVE);
        Map<Long, Lobby> myLobbies = lobbyRepository.findAllById(myPlayers.stream().map(LobbyPlayer::getLobbyId).toList())
                .stream().collect(Collectors.toMap(Lobby::getId, l -> l));
        int reviewsToGive = (int) myPlayers.stream()
                .filter(p -> p.getStatus() != PlayerStatus.NO_SHOW)
                .map(p -> myLobbies.get(p.getLobbyId()))
                .filter(l -> l != null && l.getStatus() == LobbyStatus.FINISHED)
                .filter(l -> !ratingService.hasReviewed(l.getId(), userId))
                .count();
        int paymentsToMake = (int) myPlayers.stream()
                .filter(p -> p.getStatus() == PlayerStatus.JOINED && !p.isPaid())
                .map(p -> myLobbies.get(p.getLobbyId()))
                .filter(l -> l != null && l.getStatus() == LobbyStatus.LOCKED)
                .filter(l -> l.getFinalPricePerPlayer() != null && l.getFinalPricePerPlayer().signum() > 0)
                .count();
        return new TodoCounts(pendingRequests, attendanceToSave, reviewsToGive, paymentsToMake,
                pendingRequests + attendanceToSave + reviewsToGive + paymentsToMake);
    }

    // ---------- profil și prieteni (folosite de A) ----------

    // Pe profilul meu: ultimele mele meciuri. Pe profilul altcuiva: doar meciurile jucate împreună.
    public List<LobbySummary> byUser(Long profileUserId, Long viewerId, int limit) {
        Set<Long> ids = playedLobbyIds(profileUserId);
        if (!profileUserId.equals(viewerId)) {
            ids.retainAll(playedLobbyIds(viewerId));
        }
        return lobbyRepository.findAllById(ids).stream()
                .filter(l -> l.getStatus() != LobbyStatus.CANCELLED)
                .sorted(Comparator.comparing(Lobby::getStartAt).reversed())
                .limit(Math.max(1, limit))
                .map(l -> toSummary(l, viewerId))
                .toList();
    }

    // Meciuri jucate (încheiate) și, pe profilul altcuiva, câte dintre ele împreună cu mine.
    public LobbyStats stats(Long profileUserId, Long viewerId) {
        Set<Long> played = finishedPlayedIds(profileUserId);
        Integer together = null;
        if (!profileUserId.equals(viewerId)) {
            Set<Long> both = new HashSet<>(played);
            both.retainAll(finishedPlayedIds(viewerId));
            together = both.size();
        }
        return new LobbyStats(played.size(), together);
    }

    // Pentru friends (A), „Posibili cunoscuți": cu cine am jucat în lobby-uri.
    public Set<Long> coPlayerIds(Long userId) {
        return playedLobbyIds(userId).stream()
                .flatMap(lobbyId -> lobbyPlayerRepository.findByLobbyIdAndStatusIn(lobbyId, PLAYED).stream())
                .map(LobbyPlayer::getUserId)
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toCollection(HashSet::new));
    }

    // lobby-urile în care utilizatorul a jucat sau joacă (fără cei care au ieșit sau au lipsit)
    private Set<Long> playedLobbyIds(Long userId) {
        return lobbyPlayerRepository.findByUserIdAndStatusIn(userId, PLAYED).stream()
                .map(LobbyPlayer::getLobbyId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<Long> finishedPlayedIds(Long userId) {
        return lobbyRepository.findAllById(playedLobbyIds(userId)).stream()
                .filter(l -> l.getStatus() == LobbyStatus.FINISHED)
                .map(Lobby::getId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private List<Lobby> lobbiesIPlay(Long userId) {
        List<Long> ids = lobbyPlayerRepository.findByUserIdAndStatusIn(userId, ACTIVE).stream()
                .map(LobbyPlayer::getLobbyId)
                .toList();
        return lobbyRepository.findAllById(ids);
    }

    public LobbyDetails getDetails(Long lobbyId, Long userId) {
        return toDetails(getLobby(lobbyId), userId);
    }

    private LobbyDetails toDetails(Lobby lobby, Long userId) {
        LocalDateTime now = LocalDateTime.now(ZONE);
        SportDto sport = catalogService.findSport(lobby.getSportId()).orElseThrow();
        FieldDto field = catalogService.findField(lobby.getFieldId()).orElseThrow();
        CityDto city = catalogService.findCity(lobby.getCityId()).orElseThrow();
        String hostUsername = userService.getSummary(lobby.getCreatorId()).username();

        boolean open = lobby.getStatus() == LobbyStatus.OPEN;
        boolean locked = lobby.getStatus() == LobbyStatus.LOCKED;
        boolean deadlinePassed = now.isAfter(lobby.getLockDeadline());

        List<LobbyDetails.Participant> participants = lobbyPlayerRepository
                .findByLobbyIdAndStatusIn(lobby.getId(), ACTIVE).stream()
                .map(p -> toParticipant(p, lobby, locked))
                .toList();

        int occupied = priceCalculator.occupiedSlots(lobby.getGuestCount(), participants.size());
        int freeSlots = Math.max(0, lobby.getCapacity() - occupied);

        Integer estimatedPrice = open && occupied > 0
                ? toLei(priceCalculator.pricePerPlayer(lobby.getFieldCost(), occupied))
                : null;
        Integer priceAtFull = open ? toLei(priceCalculator.pricePerPlayer(lobby.getFieldCost(), lobby.getCapacity())) : null;
        Integer finalPrice = locked ? toLei(lobby.getFinalPricePerPlayer()) : null;

        boolean isHost = lobby.getCreatorId().equals(userId);

        return new LobbyDetails(
                lobby.getId(),
                new LobbyDetails.SportRef(sport.id(), sport.code(), sport.name()),
                new LobbyDetails.FieldRef(field.id(), field.name(), field.district()),
                new LobbyDetails.CityRef(city.id(), city.name()),
                lobby.getStartAt(),
                lobby.getStatus().name(),
                new LobbyDetails.HostRef(lobby.getCreatorId(), hostUsername),
                lobby.getCapacity(),
                lobby.getMinPlayers(),
                lobby.getGuestCount(),
                occupied,
                freeSlots,
                averageRating(participants.stream().map(LobbyDetails.Participant::rating).toList()),
                lobby.getFieldCost().intValue(),
                lobby.getLockDeadline(),
                estimatedPrice,
                priceAtFull,
                finalPrice,
                open && deadlinePassed && occupied < lobby.getMinPlayers(),
                !open || freeSlots == 0 || deadlinePassed,
                lobby.isAttendanceSaved(),
                myState(lobby, userId),
                isHost,
                participants,
                isHost ? pendingRequests(lobby.getId()) : List.of());
    }

    // Cererile în așteptare, doar pentru organizator, cu ratingul fiecăruia
    private List<LobbyDetails.PendingRequest> pendingRequests(Long lobbyId) {
        return joinRequestRepository.findByLobbyIdAndStatusOrderByCreatedAtAsc(lobbyId, JoinRequestStatus.PENDING)
                .stream()
                .map(r -> {
                    RatingStatsDto stats = ratingService.getStats(r.getUserId());
                    return new LobbyDetails.PendingRequest(r.getId(), r.getUserId(),
                            userService.getSummary(r.getUserId()).username(),
                            stats.average(), stats.reliabilityPercent());
                })
                .toList();
    }

    private LobbyDetails.Participant toParticipant(LobbyPlayer player, Lobby lobby, boolean locked) {
        String username = userService.getSummary(player.getUserId()).username();
        RatingStatsDto stats = ratingService.getStats(player.getUserId());
        return new LobbyDetails.Participant(
                player.getUserId(),
                username,
                player.getUserId().equals(lobby.getCreatorId()),
                stats.average(),
                stats.reviewCount(),
                stats.reliabilityPercent(),
                locked ? player.getPaymentMethod().name() : null,
                switch (player.getStatus()) {
                    case ATTENDED -> true;
                    case NO_SHOW -> false;
                    default -> null;
                });
    }

    // Media ratingurilor participanților care au deja rating, cu o zecimală.
    private Double averageRating(List<Double> ratings) {
        OptionalDouble avg = ratings.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10) / 10.0 : null;
    }

    // Starea mea în lobby: ACCEPTED (sunt în el), PENDING (am cerut), LEFT (am ieșit),
    // REJECTED (organizatorul m-a refuzat) sau NONE. Ordinea contează: cine a ieșit și cere din nou e PENDING.
    private String myState(Lobby lobby, Long userId) {
        PlayerStatus player = lobbyPlayerRepository.findByLobbyIdAndUserId(lobby.getId(), userId)
                .map(LobbyPlayer::getStatus)
                .orElse(null);
        JoinRequestStatus request = joinRequestRepository
                .findFirstByLobbyIdAndUserIdOrderByCreatedAtDesc(lobby.getId(), userId)
                .map(JoinRequest::getStatus)
                .orElse(null);

        if (player == PlayerStatus.JOINED || player == PlayerStatus.ATTENDED || player == PlayerStatus.NO_SHOW) {
            return "ACCEPTED";
        }
        if (request == JoinRequestStatus.PENDING) return "PENDING";
        if (player == PlayerStatus.CANCELLED_FREE || player == PlayerStatus.CANCELLED_LATE) return "LEFT";
        if (request == JoinRequestStatus.REJECTED) return "REJECTED";
        return "NONE";
    }

    private static Integer toLei(BigDecimal amount) {
        return amount == null ? null : amount.intValue();
    }
}
