package ro.utcn.sportbuddy.common.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ro.utcn.sportbuddy.common.error.ApiException;

public final class CurrentUser {

    private CurrentUser() {}

    public static Long id() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof Long id)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Autentificare necesară.");
        }
        return id;
    }
}