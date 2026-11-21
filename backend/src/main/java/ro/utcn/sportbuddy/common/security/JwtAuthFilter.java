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

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            // token-ul trebuie să fie emis după ultima schimbare de parolă (versiunea din token = cea din cont)
            jwtService.parseSession(header.substring(7))
                    .filter(s -> userService.isSessionValid(s.userId(), s.version()))
                    .ifPresent(s -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(s.userId(), null, List.of())));
        }
        chain.doFilter(request, response);
    }
}