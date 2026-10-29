package ro.utcn.sportbuddy.friends.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ro.utcn.sportbuddy.friends.model.DirectMessage;

public record SendDirectMessage(
        @NotBlank(message = "Scrie un mesaj.")
        @Size(max = DirectMessage.MAX_TEXT, message = "Mesajul poate avea maximum 1000 de caractere.") String message) {}
