package ro.utcn.sportbuddy.users.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.catalog.dto.SportDto;
import ro.utcn.sportbuddy.catalog.service.CatalogService;
import ro.utcn.sportbuddy.users.model.SportLevel;
import ro.utcn.sportbuddy.users.model.User;
import ro.utcn.sportbuddy.users.model.UserSport;
import ro.utcn.sportbuddy.users.repository.UserRepository;
import ro.utcn.sportbuddy.users.repository.UserSportRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Profile("dev")
public class UserSeeder implements CommandLineRunner {

    // username, nume, vârstă, sporturi (cod:nivel)
    private static final String[][] DEMO = {
            { "ana", "Ana Popescu", "21", "football:INTERMEDIATE", "tennis:BEGINNER" },
            { "andrei", "Andrei Ionescu", "23", "football:ADVANCED", "basketball:INTERMEDIATE" },
            { "ioana", "Ioana Marin", "20", "tennis:INTERMEDIATE", "volleyball:BEGINNER" },
            { "vlad", "Vlad Pop", "25", "football:INTERMEDIATE", "running:BEGINNER" },
            { "radu", "Radu Moldovan", "22", "basketball:ADVANCED", "football:BEGINNER" },
            { "elena", "Elena Rusu", "24", "volleyball:ADVANCED", "tennis:INTERMEDIATE" },
            { "mihai", "Mihai Dumitru", "27", "running:ADVANCED", "cycling:INTERMEDIATE" },
            { "cristina", "Cristina Toma", "19", "volleyball:INTERMEDIATE", "swimming:BEGINNER" },
            { "bogdan", "Bogdan Pop", "26", "padel:BEGINNER", "football:INTERMEDIATE" },
            { "maria", "Maria Stan", "22", "running:INTERMEDIATE", "badminton:BEGINNER" },
            { "stefan", "Ștefan Luca", "28", "basketball:INTERMEDIATE", "squash:BEGINNER" },
            { "diana", "Diana Avram", "21", "running:BEGINNER", "tennis:BEGINNER" }
    };

    private final UserRepository userRepository;
    private final UserSportRepository userSportRepository;
    private final PasswordEncoder passwordEncoder;
    private final CatalogService catalogService;

    public UserSeeder(UserRepository userRepository, UserSportRepository userSportRepository,
                      PasswordEncoder passwordEncoder, CatalogService catalogService) {
        this.userRepository = userRepository;
        this.userSportRepository = userSportRepository;
        this.passwordEncoder = passwordEncoder;
        this.catalogService = catalogService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0)
            return;

        String hash = passwordEncoder.encode("demo123");
        List<User> users = new ArrayList<>();
        for (String[] d : DEMO) {
            User u = new User();
            u.setUsername(d[0]);
            u.setFullName(d[1]);
            u.setEmail(d[0] + "@demo.ro");
            u.setPasswordHash(hash);
            u.setAge(Integer.parseInt(d[2]));
            users.add(u);
        }
        userRepository.saveAll(users);
    }

    // După pornire (catalogul e sigur creat): sporturi pentru utilizatorii demo care nu au încă niciunul.
    // Merge și pe bazele de date create înainte, fără să atingă sporturile alese deja de cineva.
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedSports() {
        Map<String, Long> sports = catalogService.getSports().stream()
                .collect(Collectors.toMap(SportDto::code, SportDto::id));
        for (String[] d : DEMO) {
            userRepository.findByUsernameIgnoreCase(d[0])
                    .filter(u -> userSportRepository.findByUserId(u.getId()).isEmpty())
                    .ifPresent(u -> {
                        for (int i = 3; i < d.length; i++) {
                            String[] sportLevel = d[i].split(":");
                            Long sportId = sports.get(sportLevel[0]);
                            if (sportId != null) {
                                userSportRepository.save(
                                        new UserSport(u.getId(), sportId, SportLevel.valueOf(sportLevel[1])));
                            }
                        }
                    });
        }
    }
}
