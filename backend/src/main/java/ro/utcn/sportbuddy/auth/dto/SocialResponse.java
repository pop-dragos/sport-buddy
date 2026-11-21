package ro.utcn.sportbuddy.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ro.utcn.sportbuddy.users.dto.UserMe;

import java.time.LocalDateTime;

// Răspunsul la conectarea cu Google / iCloud: ori conectat, ori trebuie terminată înregistrarea
// (profile = datele de la furnizor; signupToken doar pentru Google real, se trimite la /auth/register).
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SocialResponse(String token, LocalDateTime expiresAt, UserMe user,
                             Boolean needsSignup, Profile profile, String signupToken) {

    public record Profile(String fullName, String email) {}

    public static SocialResponse loggedIn(AuthResponse r) {
        return new SocialResponse(r.token(), r.expiresAt(), r.user(), null, null, null);
    }

    public static SocialResponse signupNeeded(Profile profile) {
        return signupNeeded(profile, null);
    }

    public static SocialResponse signupNeeded(Profile profile, String signupToken) {
        return new SocialResponse(null, null, null, true, profile, signupToken);
    }
}
