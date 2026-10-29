package ro.utcn.sportbuddy.feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ro.utcn.sportbuddy.feed.model.Post;
import ro.utcn.sportbuddy.feed.model.PostTag;

public record CreatePostRequest(
        @NotBlank(message = "Scrie ceva în postare.")
        @Size(max = Post.MAX_TEXT, message = "Postarea poate avea maximum 1000 de caractere.") String content,
        @NotNull(message = "Alege sportul.") Long sportId,
        @NotNull(message = "Alege tipul postării.") PostTag tag) {}
