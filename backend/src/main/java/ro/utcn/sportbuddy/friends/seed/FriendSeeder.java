package ro.utcn.sportbuddy.friends.seed;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.utcn.sportbuddy.friends.model.DirectMessage;
import ro.utcn.sportbuddy.friends.model.Friendship;
import ro.utcn.sportbuddy.friends.repository.DirectMessageRepository;
import ro.utcn.sportbuddy.friends.repository.FriendshipRepository;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

// Câteva prietenii și cereri în jurul Anei, ca pagina Prieteni să nu fie goală la prima rulare.
@Component
@Profile("dev")
public class FriendSeeder {

    // requester, addressee, acceptată?
    private static final Object[][] FRIENDSHIPS = {
            { "ana", "andrei", true }, { "ioana", "ana", true }, { "ana", "elena", true },
            { "andrei", "ioana", true }, { "vlad", "ana", false }, { "ana", "radu", false }
    };

    // expeditor, destinatar, minute în urmă, text
    private static final Object[][] MESSAGES = {
            { "andrei", "ana", 50, "Salut. Joci sâmbătă la fotbal?" },
            { "ana", "andrei", 45, "Da, la 19. Mai aduci pe cineva?" },
            { "andrei", "ana", 40, "Vine și Vlad. Ne vedem acolo." }
    };

    private final FriendshipRepository friendshipRepository;
    private final DirectMessageRepository messageRepository;
    private final UserService userService;

    public FriendSeeder(FriendshipRepository friendshipRepository, DirectMessageRepository messageRepository,
                        UserService userService) {
        this.friendshipRepository = friendshipRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (friendshipRepository.count() > 0) return;

        for (Object[] f : FRIENDSHIPS) {
            Optional<Long> from = userService.findIdByUsername((String) f[0]);
            Optional<Long> to = userService.findIdByUsername((String) f[1]);
            if (from.isEmpty() || to.isEmpty()) continue;
            Friendship friendship = new Friendship(from.get(), to.get());
            if ((Boolean) f[2]) friendship.accept();
            friendshipRepository.save(friendship);
        }

        Instant now = Instant.now();
        for (Object[] m : MESSAGES) {
            Optional<Long> from = userService.findIdByUsername((String) m[0]);
            Optional<Long> to = userService.findIdByUsername((String) m[1]);
            if (from.isEmpty() || to.isEmpty()) continue;
            DirectMessage message = new DirectMessage(from.get(), to.get(), (String) m[3]);
            message.setSentAt(now.minus(Duration.ofMinutes((Integer) m[2])));
            messageRepository.save(message);
        }
    }
}
