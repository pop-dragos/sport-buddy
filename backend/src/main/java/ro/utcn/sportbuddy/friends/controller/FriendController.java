package ro.utcn.sportbuddy.friends.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.friends.dto.*;
import ro.utcn.sportbuddy.friends.service.FriendService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public List<FriendItem> friends(@RequestParam(required = false) String query) {
        return friendService.listFriends(CurrentUser.id(), query);
    }

    @GetMapping("/of/{userId}")
    public List<FriendItem> friendsOf(@PathVariable Long userId) {
        return friendService.friendsOf(CurrentUser.id(), userId);
    }

    @GetMapping("/suggestions")
    public List<FriendItem> suggestions() {
        return friendService.suggestions(CurrentUser.id());
    }

    @GetMapping("/requests")
    public FriendRequests requests() {
        return friendService.requests(CurrentUser.id());
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendStatus sendRequest(@Valid @RequestBody SendFriendRequest req) {
        return friendService.sendRequest(CurrentUser.id(), req.userId());
    }

    @PostMapping("/requests/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void accept(@PathVariable Long id) {
        friendService.accept(CurrentUser.id(), id);
    }

    @PostMapping("/requests/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable Long id) {
        friendService.reject(CurrentUser.id(), id);
    }

    @DeleteMapping("/requests/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        friendService.cancel(CurrentUser.id(), id);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long userId) {
        friendService.removeFriend(CurrentUser.id(), userId);
    }

    // ?ids=1,2,3: starea față de fiecare (de exemplu, participanții unui lobby), într-o singură cerere
    @GetMapping("/statuses")
    public Map<Long, FriendStatus> statuses(@RequestParam List<Long> ids) {
        return friendService.statuses(CurrentUser.id(), ids);
    }

        @GetMapping("/status/{userId}")
    public FriendStatus status(@PathVariable Long userId) {
        return friendService.status(CurrentUser.id(), userId);
    }

    @GetMapping("/{userId}/messages")
    public List<DirectMessageDto> messages(@PathVariable Long userId, @RequestParam(required = false) Long after) {
        return friendService.messages(CurrentUser.id(), userId, after);
    }

    @PostMapping("/{userId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public DirectMessageDto send(@PathVariable Long userId, @Valid @RequestBody SendDirectMessage req) {
        return friendService.sendMessage(CurrentUser.id(), userId, req.message());
    }
}
