package ro.utcn.sportbuddy.users.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.utcn.sportbuddy.users.model.User;
import ro.utcn.sportbuddy.users.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
public class UserSeeder implements CommandLineRunner {

    private static final String[][] DEMO = {
            { "ana", "Ana Popescu", "21" }, { "andrei", "Andrei Ionescu", "23" },
            { "ioana", "Ioana Marin", "20" }, { "vlad", "Vlad Pop", "25" },
            { "radu", "Radu Moldovan", "22" }, { "elena", "Elena Rusu", "24" },
            { "mihai", "Mihai Dumitru", "27" }, { "cristina", "Cristina Toma", "19" },
            { "bogdan", "Bogdan Pop", "26" }, { "maria", "Maria Stan", "22" },
            { "stefan", "Ștefan Luca", "28" }, { "diana", "Diana Avram", "21" }
    };

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}