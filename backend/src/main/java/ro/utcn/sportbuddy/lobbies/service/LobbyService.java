package ro.utcn.sportbuddy.lobbies.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.catalog.dto.CityDto;
import ro.utcn.sportbuddy.catalog.dto.FieldDto;
import ro.utcn.sportbuddy.catalog.dto.SportDto;
import ro.utcn.sportbuddy.catalog.service.CatalogService;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.lobbies.dto.CalendarDays;
import ro.utcn.sportbuddy.lobbies.dto.CreateLobbyRequest;
import ro.utcn.sportbuddy.lobbies.dto.LobbyDetails;
import ro.utcn.sportbuddy.lobbies.dto.LobbySummary;
import ro.utcn.sportbuddy.lobbies.model.Lobby;
import ro.utcn.sportbuddy.lobbies.model.LobbyPlayer;
import ro.utcn.sportbuddy.lobbies.model.LobbyStatus;
import ro.utcn.sportbuddy.lobbies.model.PlayerStatus;
import ro.utcn.sportbuddy.lobbies.repository.LobbyPlayerRepository;
import ro.utcn.sportbuddy.lobbies.repository.LobbyRepository;
import ro.utcn.sportbuddy.ratings.dto.RatingStatsDto;
import ro.utcn.sportbuddy.ratings.service.RatingService;
import ro.utcn.sportbuddy.users.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;

@Service
public class LobbyService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");

    private final LobbyRepository lobbyRepository;
    private final LobbyPlayerRepository lobbyPlayerRepository;
    private final CatalogService catalogService;
    private final UserService userService;
    private final RatingService ratingService;
    private final LockDeadlineCalculator deadlineCalculator;
    private final PriceCalculator priceCalculator;

    public LobbyService(LobbyRepository lobbyRepository, LobbyPlayerRepository lobbyPlayerRepository,
            CatalogService catalogService, UserService userService, RatingService ratingService,
            LockDeadlineCalculator deadlineCalculator, PriceCalculator priceCalculator) {
        this.lobbyRepository = lobbyRepository;
        this.lobbyPlayerRepository = lobbyPlayerRepository;
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

        List<LobbyPlayer> joined = lobbyPlayerRepository.findByLobbyIdAndStatus(lobby.getId(), PlayerStatus.JOINED);
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
                myState(lobby, userId));
    }

    public LobbyDetails getDetails(Long lobbyId, Long userId) {
        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Lobby-ul nu există."));
        return toDetails(lobby, userId);
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
                .findByLobbyIdAndStatus(lobby.getId(), PlayerStatus.JOINED).stream()
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
                myState(lobby, userId),
                isHost,
                participants,
                List.of()); // cererile de alăturare vin odată cu JoinRequest
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
                locked ? player.getPaymentMethod().name() : null);
    }

    // Media ratingurilor participanților care au deja rating, cu o zecimală.
    private Double averageRating(List<Double> ratings) {
        OptionalDouble avg = ratings.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10) / 10.0 : null;
    }

    // NONE (nu are legătură cu lobby-ul), ACCEPTED (e în lobby), LEFT (a ieșit).
    // PENDING și REJECTED apar odată cu cererile de alăturare.
    private String myState(Lobby lobby, Long userId) {
        return lobbyPlayerRepository.findByLobbyIdAndUserId(lobby.getId(), userId)
                .map(p -> p.getStatus() == PlayerStatus.CANCELLED_FREE || p.getStatus() == PlayerStatus.CANCELLED_LATE
                        ? "LEFT"
                        : "ACCEPTED")
                .orElse("NONE");
    }

    private static Integer toLei(BigDecimal amount) {
        return amount == null ? null : amount.intValue();
    }
}
