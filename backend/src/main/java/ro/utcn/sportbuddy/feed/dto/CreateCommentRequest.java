package ro.utcn.sportbuddy.feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ro.utcn.sportbuddy.feed.model.Comment;

public record CreateCommentRequest(
        @NotBlank(message = "Scrie un comentariu.")
        @Size(max = Comment.MAX_TEXT, message = "Comentariul poate avea maximum 500 de caractere.") String content) {}
