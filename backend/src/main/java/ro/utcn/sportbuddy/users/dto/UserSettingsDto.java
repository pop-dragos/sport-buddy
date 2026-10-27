package ro.utcn.sportbuddy.users.dto;

import jakarta.validation.constraints.NotNull;
import ro.utcn.sportbuddy.users.model.ProfileVisibility;
import ro.utcn.sportbuddy.users.model.Theme;
import ro.utcn.sportbuddy.users.model.WhoCanAddMe;

// Nu există câmp pentru rating: ratingul nu se poate ascunde.
public record UserSettingsDto(
        @NotNull Theme theme,
        @NotNull Boolean showWeather,
        @NotNull Boolean notifyJoinRequests,
        @NotNull Boolean notifyMessages,
        @NotNull Boolean notifyReminder,
        @NotNull Boolean notifyWeeklyDigest,
        @NotNull ProfileVisibility profileVisibility,
        @NotNull WhoCanAddMe whoCanAddMe,
        @NotNull Boolean showFriendsList) {}
