package ro.utcn.sportbuddy.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ro.utcn.sportbuddy.users.dto.UserMe;

import java.time.LocalDateTime;

// Ori utilizatorul e conectat direct (cont Google, emailul e deja confirmat),
// ori trebuie să-și confirme emailul din linkul primit (needsVerification). Până atunci nu are sesiune,
// așa că primește setupToken: valabil o oră, doar pentru poza de profil aleasă în formular.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RegisterResponse(String token, LocalDateTime expiresAt, UserMe user,
                               Boolean needsVerification, String email, String setupToken) {

    public static RegisterResponse loggedIn(AuthResponse r) {
        return new RegisterResponse(r.token(), r.expiresAt(), r.user(), null, null, null);
    }

    public static RegisterResponse verificationSent(String email, String setupToken) {
        return new RegisterResponse(null, null, null, true, email, setupToken);
    }
}
