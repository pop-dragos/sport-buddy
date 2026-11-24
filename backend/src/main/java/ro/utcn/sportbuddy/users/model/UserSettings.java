package ro.utcn.sportbuddy.users.model;

import jakarta.persistence.*;
import ro.utcn.sportbuddy.common.i18n.Language;

// Cheia primară este chiar userId (relație 1-1 cu User, fără @OneToOne: legătura se face prin id).
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Theme theme = Theme.LIGHT;

    private boolean showWeather = true;
    private boolean notifJoinRequests = true;
    private boolean notifMessages = true;
    private boolean notifReminders = true;
    private boolean notifEmailDigest = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProfileVisibility profileVisibility = ProfileVisibility.EVERYONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WhoCanAddMe whoCanAddMe = WhoCanAddMe.EVERYONE;

    private boolean showFriendList = true;

    // Coloanele pot lipsi la conturile vechi (null în baza de date): getter-ele dau atunci valoarea implicită,
    // așa că nu e nevoie de o migrare.
    @Enumerated(EnumType.STRING)
    private Language language;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    protected UserSettings() {}

    public UserSettings(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() { return userId; }

    public Theme getTheme() { return theme; }
    public void setTheme(Theme theme) { this.theme = theme; }

    public boolean isShowWeather() { return showWeather; }
    public void setShowWeather(boolean showWeather) { this.showWeather = showWeather; }

    public boolean isNotifJoinRequests() { return notifJoinRequests; }
    public void setNotifJoinRequests(boolean notifJoinRequests) { this.notifJoinRequests = notifJoinRequests; }

    public boolean isNotifMessages() { return notifMessages; }
    public void setNotifMessages(boolean notifMessages) { this.notifMessages = notifMessages; }

    public boolean isNotifReminders() { return notifReminders; }
    public void setNotifReminders(boolean notifReminders) { this.notifReminders = notifReminders; }

    public boolean isNotifEmailDigest() { return notifEmailDigest; }
    public void setNotifEmailDigest(boolean notifEmailDigest) { this.notifEmailDigest = notifEmailDigest; }

    public ProfileVisibility getProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(ProfileVisibility profileVisibility) { this.profileVisibility = profileVisibility; }

    public WhoCanAddMe getWhoCanAddMe() { return whoCanAddMe; }
    public void setWhoCanAddMe(WhoCanAddMe whoCanAddMe) { this.whoCanAddMe = whoCanAddMe; }

    public Language getLanguage() { return language == null ? Language.RO : language; }
    public void setLanguage(Language language) { this.language = language; }

    public Currency getCurrency() { return currency == null ? Currency.RON : currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }

    public boolean isShowFriendList() { return showFriendList; }
    public void setShowFriendList(boolean showFriendList) { this.showFriendList = showFriendList; }
}
