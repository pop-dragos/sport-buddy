package ro.utcn.sportbuddy.ratings.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// POST /lobbies/{id}/reviews: { "reviews": [ { "userId": 21, "score": 4 } ] }
public record SubmitReviewsRequest(
        @NotEmpty(message = "Dă cel puțin o notă.") List<@Valid Item> reviews) {

    public record Item(
            @NotNull(message = "Alege colegul.") Long userId,
            @NotNull(message = "Alege nota.")
            @Min(value = 1, message = "Nota trebuie să fie între 1 și 5.")
            @Max(value = 5, message = "Nota trebuie să fie între 1 și 5.") Integer score) {
    }
}
