package ro.utcn.sportbuddy.lobbies.seed;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.catalog.dto.CityDto;
import ro.utcn.sportbuddy.catalog.dto.FieldDto;
import ro.utcn.sportbuddy.catalog.dto.SportDto;
import ro.utcn.sportbuddy.catalog.service.CatalogService;
import ro.utcn.sportbuddy.lobbies.model.JoinRequest;
import ro.utcn.sportbuddy.lobbies.model.Lobby;
import ro.utcn.sportbuddy.lobbies.model.LobbyPlayer;
import ro.utcn.sportbuddy.lobbies.model.LobbyStatus;
import ro.utcn.sportbuddy.lobbies.model.PlayerStatus;
import ro.utcn.sportbuddy.lobbies.repository.JoinRequestRepository;
import ro.utcn.sportbuddy.lobbies.repository.LobbyPlayerRepository;
import ro.utcn.sportbuddy.lobbies.repository.LobbyRepository;
import ro.utcn.sportbuddy.lobbies.service.LockDeadlineCalculator;
import ro.utcn.sportbuddy.lobbies.service.PriceCalculator;
import ro.utcn.sportbuddy.users.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Lobby-uri de test, relative la ziua de azi, ca datele să fie mereu actuale după o resetare a bazei.
// Doar pe profilul dev și doar dacă tabela e goală.
@Component
@Profile("dev")
public class LobbySeeder {

    private static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");

    // days: față de azi (negativ = în trecut); guests: jucători aduși de organizator, fără cont
    private record Demo(String host, String sport, String city, int days, int hour, int minute,
                        int capacity, int minPlayers, int guests, int cost,
                        List<String> players, List<String> pending, LobbyStatus status, boolean attendanceSaved) {}

    private static final List<Demo> DEMOS = List.of(
            // Baia Mare, viitoare
            new Demo("vlad", "football", "Baia Mare", 1, 18, 0, 10, 6, 2, 200,
                    List.of("andrei", "bogdan", "radu"), List.of("mihai"), LobbyStatus.OPEN, false),
            new Demo("radu", "football", "Baia Mare", 1, 20, 0, 10, 6, 4, 200,
                    List.of("andrei", "vlad", "ana"), List.of(), LobbyStatus.LOCKED, false),
            new Demo("ana", "tennis", "Baia Mare", 2, 10, 0, 2, 2, 0, 80,
                    List.of(), List.of("ioana", "diana"), LobbyStatus.OPEN, false),
            new Demo("radu", "basketball", "Baia Mare", 2, 20, 0, 10, 6, 3, 150,
                    List.of("stefan", "andrei"), List.of(), LobbyStatus.OPEN, false),
            new Demo("andrei", "football", "Baia Mare", 3, 19, 30, 12, 8, 0, 240,
                    List.of("vlad", "radu", "bogdan", "stefan", "mihai"), List.of(), LobbyStatus.OPEN, false),
            new Demo("elena", "volleyball", "Baia Mare", 4, 18, 0, 12, 8, 2, 0,
                    List.of("ioana", "cristina", "ana"), List.of(), LobbyStatus.OPEN, false),
            new Demo("mihai", "running", "Baia Mare", 5, 8, 0, 10, 2, 0, 0,
                    List.of("maria", "diana", "vlad"), List.of(), LobbyStatus.OPEN, false),
            new Demo("bogdan", "padel", "Baia Mare", 6, 17, 0, 4, 4, 0, 120,
                    List.of("ana"), List.of(), LobbyStatus.OPEN, false),
            // alte orașe
            new Demo("stefan", "football", "Cluj-Napoca", 2, 18, 0, 10, 6, 5, 250,
                    List.of("mihai"), List.of(), LobbyStatus.OPEN, false),
            new Demo("diana", "tennis", "Cluj-Napoca", 3, 9, 0, 2, 2, 0, 60,
                    List.of(), List.of(), LobbyStatus.OPEN, false),
            new Demo("cristina", "swimming", "Satu Mare", 2, 7, 30, 6, 2, 1, 0,
                    List.of("elena"), List.of(), LobbyStatus.OPEN, false),
            // încheiate: unul cu prezența de bifat (pentru ana), unul cu prezența salvată
            new Demo("ana", "football", "Baia Mare", -2, 18, 0, 10, 6, 3, 200,
                    List.of("andrei", "vlad", "radu"), List.of(), LobbyStatus.FINISHED, false),
            new Demo("andrei", "basketball", "Baia Mare", -5, 19, 0, 10, 6, 4, 150,
                    List.of("ana", "radu", "stefan"), List.of(), LobbyStatus.FINISHED, true)
    );

    private final LobbyRepository lobbyRepository;
    private final LobbyPlayerRepository lobbyPlayerRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final CatalogService catalogService;
    private final UserService userService;
    private final LockDeadlineCalculator deadlineCalculator;
    private final PriceCalculator priceCalculator;

    public LobbySeeder(LobbyRepository lobbyRepository, LobbyPlayerRepository lobbyPlayerRepository,
            JoinRequestRepository joinRequestRepository, CatalogService catalogService, UserService userService,
            LockDeadlineCalculator deadlineCalculator, PriceCalculator priceCalculator) {
        this.lobbyRepository = lobbyRepository;
        this.lobbyPlayerRepository = lobbyPlayerRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.catalogService = catalogService;
        this.userService = userService;
        this.deadlineCalculator = deadlineCalculator;
        this.priceCalculator = priceCalculator;
    }

    // ApplicationReadyEvent vine după toate seeder-ele de tip CommandLineRunner: utilizatorii și catalogul există.
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (lobbyRepository.count() > 0)
            return;

        Map<String, Long> sports = catalogService.getSports().stream()
                .collect(Collectors.toMap(SportDto::code, SportDto::id));
        LocalDateTime now = LocalDateTime.now(ZONE);

        for (Demo d : DEMOS) {
            Long hostId = userId(d.host());
            Long cityId = cityId(d.city());
            Long sportId = sports.get(d.sport());
            if (hostId == null || cityId == null || sportId == null)
                continue;
            List<FieldDto> fields = catalogService.getFields(cityId, sportId);
            if (fields.isEmpty())
                continue;
            create(d, hostId, cityId, sportId, fields.get(0).id(), now);
        }
    }

    private void create(Demo d, Long hostId, Long cityId, Long sportId, Long fieldId, LocalDateTime now) {
        LocalDateTime startAt = now.toLocalDate().plusDays(d.days()).atTime(d.hour(), d.minute());
        boolean past = d.days() < 0;

        Lobby lobby = new Lobby();
        lobby.setCreatorId(hostId);
        lobby.setSportId(sportId);
        lobby.setCityId(cityId);
        lobby.setFieldId(fieldId);
        lobby.setStartAt(startAt);
        lobby.setCapacity(d.capacity());
        lobby.setMinPlayers(d.minPlayers());
        lobby.setFieldCost(BigDecimal.valueOf(d.cost()));
        lobby.setGuestCount(d.guests());
        // pentru meciurile din trecut, termenul e calculat ca și cum lobby-ul ar fi fost creat cu o săptămână înainte
        lobby.setLockDeadline(deadlineCalculator.deadline(past ? startAt.minusDays(7) : now, startAt));
        lobby.setStatus(d.status());
        if (d.status() != LobbyStatus.OPEN) {
            int occupied = d.guests() + 1 + d.players().size();
            lobby.setFinalPricePerPlayer(priceCalculator.pricePerPlayer(lobby.getFieldCost(), occupied));
            lobby.setLockedAt(lobby.getLockDeadline());
        }
        lobby.setAttendanceSaved(d.attendanceSaved());
        lobby = lobbyRepository.save(lobby);

        PlayerStatus playerStatus = d.attendanceSaved() ? PlayerStatus.ATTENDED : PlayerStatus.JOINED;
        addPlayer(lobby.getId(), hostId, playerStatus);
        for (String username : d.players()) {
            Long id = userId(username);
            if (id != null)
                addPlayer(lobby.getId(), id, playerStatus);
        }
        for (String username : d.pending()) {
            Long id = userId(username);
            if (id != null) {
                JoinRequest request = new JoinRequest();
                request.setLobbyId(lobby.getId());
                request.setUserId(id);
                joinRequestRepository.save(request);
            }
        }
    }

    private void addPlayer(Long lobbyId, Long userId, PlayerStatus status) {
        LobbyPlayer player = new LobbyPlayer();
        player.setLobbyId(lobbyId);
        player.setUserId(userId);
        player.setStatus(status);
        lobbyPlayerRepository.save(player);
    }

    private Long userId(String username) {
        return userService.findIdByUsername(username).orElse(null);
    }

    private Long cityId(String name) {
        return catalogService.searchCities(name).stream()
                .filter(c -> c.name().equals(name))
                .map(CityDto::id)
                .findFirst()
                .orElse(null);
    }
}
