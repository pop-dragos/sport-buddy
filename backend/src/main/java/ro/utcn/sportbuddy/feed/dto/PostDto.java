package ro.utcn.sportbuddy.feed.dto;

import ro.utcn.sportbuddy.feed.model.PostTag;
import ro.utcn.sportbuddy.users.dto.UserRef;

import java.time.LocalDateTime;

public record PostDto(Long id, UserRef author, Long sportId, PostTag tag, String content,
                      LocalDateTime createdAt, long likeCount, boolean likedByMe, long commentCount) {}
