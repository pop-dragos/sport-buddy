package ro.utcn.sportbuddy.feed.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.feed.dto.*;
import ro.utcn.sportbuddy.feed.service.FeedService;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public List<PostDto> feed(@RequestParam(required = false) Long sportId,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return feedService.getFeed(CurrentUser.id(), sportId, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto create(@Valid @RequestBody CreatePostRequest req) {
        return feedService.createPost(CurrentUser.id(), req.content(), req.sportId(), req.tag());
    }

    @PostMapping("/{id}/like")
    public LikeResult like(@PathVariable Long id) {
        return feedService.toggleLike(CurrentUser.id(), id);
    }

    @GetMapping("/{id}/comments")
    public List<CommentDto> comments(@PathVariable Long id) {
        return feedService.getComments(id);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto comment(@PathVariable Long id, @Valid @RequestBody CreateCommentRequest req) {
        return feedService.addComment(CurrentUser.id(), id, req.content());
    }
}
