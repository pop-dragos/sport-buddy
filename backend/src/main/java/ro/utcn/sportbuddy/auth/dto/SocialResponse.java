package ro.utcn.sportbuddy.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ro.utcn.sportbuddy.users.dto.UserMe;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SocialResponse(String token, LocalDateTime expiresAt, UserMe user,
                             Boolean needsSignup, Profile profile) {

    public record Profile(String fullName, String email) {}

    public static SocialResponse loggedIn(AuthResponse r) {
        return new SocialResponse(r.token(), r.expiresAt(), r.user(), null, null);
    }

    public static SocialResponse signupNeeded(Profile profile) {
        return new SocialResponse(null, null, null, true, profile);
    }
}
