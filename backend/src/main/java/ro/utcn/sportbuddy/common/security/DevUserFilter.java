package ro.utcn.sportbuddy.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import ro.utcn.sportbuddy.users.service.UserService;

import java.io.IOException;
import java.util.List;

public class DevUserFilter extends OncePerRequestFilter {

    private final UserService userService;

    public DevUserFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String username = request.getHeader("X-Dev-User");
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            userService.findIdByUsername(username).ifPresent(id ->
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(id, null, List.of())));
        }
        chain.doFilter(request, response);
    }
}