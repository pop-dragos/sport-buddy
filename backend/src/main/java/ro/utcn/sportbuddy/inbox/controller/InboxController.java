package ro.utcn.sportbuddy.inbox.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.inbox.dto.ConversationDto;
import ro.utcn.sportbuddy.inbox.service.InboxService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class InboxController {

    private final InboxService inboxService;

    public InboxController(InboxService inboxService) {
        this.inboxService = inboxService;
    }

    // conversațiile mele, cea mai recentă prima
    @GetMapping("/conversations")
    public List<ConversationDto> conversations() {
        return inboxService.conversations(CurrentUser.id());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", inboxService.unreadCount(CurrentUser.id()));
    }
}
