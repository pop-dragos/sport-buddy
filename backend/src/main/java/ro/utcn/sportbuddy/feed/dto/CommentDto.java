package ro.utcn.sportbuddy.feed.dto;

import ro.utcn.sportbuddy.users.dto.UserRef;

import java.time.LocalDateTime;

public record CommentDto(Long id, UserRef author, String content, LocalDateTime createdAt) {}
