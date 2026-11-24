package ro.utcn.sportbuddy.chat.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.chat.dto.ChatMessageDto;
import ro.utcn.sportbuddy.chat.dto.SendMessageRequest;
import ro.utcn.sportbuddy.chat.service.ChatService;
import ro.utcn.sportbuddy.common.security.CurrentUser;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lobbies")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{lobbyId}/messages")
    public List<ChatMessageDto> list(@PathVariable Long lobbyId,
                                     @RequestParam(required = false) Long after) {
        return chatService.getMessages(lobbyId, CurrentUser.id(), after);
    }

    @PostMapping("/{lobbyId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageDto send(@PathVariable Long lobbyId, @Valid @RequestBody SendMessageRequest req) {
        return chatService.send(lobbyId, CurrentUser.id(), req.message());
    }

    // { lobbyId: câte mesaje necitite }, doar pentru lobby-urile mele cu mesaje noi
    @GetMapping("/mine/chat-unread")
    public Map<Long, Long> unread() {
        return chatService.unreadByLobby(CurrentUser.id());
    }
}
