package ro.utcn.sportbuddy.users.dto;

import jakarta.validation.constraints.NotNull;
import ro.utcn.sportbuddy.users.model.SportLevel;

public record SportLevelDto(@NotNull Long sportId, @NotNull SportLevel level) {}