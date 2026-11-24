package ro.utcn.sportbuddy.users.dto;

import jakarta.validation.constraints.NotNull;
import ro.utcn.sportbuddy.common.i18n.Language;
import ro.utcn.sportbuddy.users.model.Currency;
import ro.utcn.sportbuddy.users.model.ProfileVisibility;
import ro.utcn.sportbuddy.users.model.Theme;
import ro.utcn.sportbuddy.users.model.WhoCanAddMe;

// Nu există câmp pentru rating: ratingul nu se poate ascunde.
// language și currency sunt opționale la salvare: lipsa lor lasă valorile neschimbate.
public record UserSettingsDto(
        @NotNull Theme theme,
        @NotNull Boolean showWeather,
        @NotNull Boolean notifyJoinRequests,
        @NotNull Boolean notifyMessages,
        @NotNull Boolean notifyReminder,
        @NotNull Boolean notifyWeeklyDigest,
        @NotNull ProfileVisibility profileVisibility,
        @NotNull WhoCanAddMe whoCanAddMe,
        @NotNull Boolean showFriendsList,
        Language language,
        Currency currency) {

    public UserSettingsDto(Theme theme, Boolean showWeather, Boolean notifyJoinRequests, Boolean notifyMessages,
                           Boolean notifyReminder, Boolean notifyWeeklyDigest, ProfileVisibility profileVisibility,
                           WhoCanAddMe whoCanAddMe, Boolean showFriendsList) {
        this(theme, showWeather, notifyJoinRequests, notifyMessages, notifyReminder, notifyWeeklyDigest,
                profileVisibility, whoCanAddMe, showFriendsList, null, null);
    }
}
