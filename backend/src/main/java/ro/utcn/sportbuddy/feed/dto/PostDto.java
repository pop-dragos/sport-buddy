package ro.utcn.sportbuddy.feed.dto;

import ro.utcn.sportbuddy.feed.model.PostTag;
import ro.utcn.sportbuddy.users.dto.UserRef;

import java.time.LocalDateTime;

// authorRating: media autorului (din ratings), gol pentru utilizatorii noi.
public record PostDto(Long id, UserRef author, Double authorRating, Long sportId, PostTag tag, String content,
                      LocalDateTime createdAt, long likeCount, boolean likedByMe, long commentCount) {}
