package ro.utcn.sportbuddy.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

// Verifică token-ul primit de browser de la butonul „Continuă cu Google” (Google Identity Services):
// semnătura cu cheile publice Google, emitentul, că e făcut pentru aplicația noastră (audience) și că nu a expirat.
@Component
public class GoogleTokenVerifier {

    public record GoogleProfile(String subject, String email, boolean emailVerified, String name) {}

    private static final String JWKS = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final String clientId;
    private volatile NimbusJwtDecoder decoder;

    public GoogleTokenVerifier(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId == null ? "" : clientId.trim();
    }

    public boolean isConfigured() {
        return !clientId.isEmpty();
    }

    public String clientId() {
        return clientId;
    }

    public Optional<GoogleProfile> verify(String credential) {
        if (!isConfigured() || credential == null || credential.isBlank()) return Optional.empty();
        try {
            Jwt jwt = decoder().decode(credential);
            return Optional.of(new GoogleProfile(jwt.getSubject(), jwt.getClaimAsString("email"),
                    Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")), jwt.getClaimAsString("name")));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    // Cheile Google se descarcă la prima folosire (și se țin în cache), nu la pornirea aplicației.
    private NimbusJwtDecoder decoder() {
        NimbusJwtDecoder d = decoder;
        if (d == null) {
            synchronized (this) {
                if (decoder == null) {
                    NimbusJwtDecoder created = NimbusJwtDecoder.withJwkSetUri(JWKS).build();
                    created.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                            new JwtTimestampValidator(),
                            check("issuer", j -> ISSUERS.contains(j.getClaimAsString("iss"))),
                            check("audience", j -> j.getAudience() != null && j.getAudience().contains(clientId))));
                    decoder = created;
                }
                d = decoder;
            }
        }
        return d;
    }

    private static OAuth2TokenValidator<Jwt> check(String what, Predicate<Jwt> ok) {
        return jwt -> ok.test(jwt) ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Google " + what + " invalid", null));
    }
}
