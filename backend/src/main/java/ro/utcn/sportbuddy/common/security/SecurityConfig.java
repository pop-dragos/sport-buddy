package ro.utcn.sportbuddy.common.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ro.utcn.sportbuddy.users.service.UserService;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain chain(HttpSecurity http, UserService userService,
                                     JwtService jwtService, Environment env) throws Exception {
        http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/api/auth/**", "/api/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/catalog/**").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) ->
                    writeError(res, 401, "UNAUTHORIZED", "Autentificare necesară."))
                .accessDeniedHandler((req, res, ex) ->
                    writeError(res, 403, "FORBIDDEN", "Nu ai dreptul la această acțiune.")));

        http.addFilterBefore(new JwtAuthFilter(jwtService, userService), UsernamePasswordAuthenticationFilter.class);
        if (env.acceptsProfiles(Profiles.of("dev"))) {
            http.addFilterBefore(new DevUserFilter(userService), UsernamePasswordAuthenticationFilter.class);
        }
        return http.build();
    }

    private static void writeError(HttpServletResponse res, int status, String code, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }
}