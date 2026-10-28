package ro.utcn.sportbuddy.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank(message = "Scrie un mesaj.")
        @Size(max = 500, message = "Mesajul poate avea maximum 500 de caractere.") String message) {
}
