package ro.utcn.sportbuddy.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.utcn.sportbuddy.auth.dto.AuthResponse;
import ro.utcn.sportbuddy.auth.dto.LoginRequest;
import ro.utcn.sportbuddy.auth.dto.RegisterRequest;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.common.security.JwtService;
import ro.utcn.sportbuddy.users.dto.SportLevelDto;
import ro.utcn.sportbuddy.users.dto.UserCredentials;
import ro.utcn.sportbuddy.users.dto.UserMe;
import ro.utcn.sportbuddy.users.model.AuthProvider;
import ro.utcn.sportbuddy.users.model.SportLevel;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-123";

    private UserService userService;
    private JwtService jwtService;
    private PasswordEncoder encoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        jwtService = new JwtService(SECRET);
        encoder = new BCryptPasswordEncoder();
        authService = new AuthService(userService, jwtService, encoder);
        when(userService.getMe(any())).thenAnswer(i -> userMe(i.getArgument(0)));
    }

    private static UserMe userMe(Long id) {
        return new UserMe(id, "ana", "Ana Popescu", "ana@exemplu.ro", null, 21, null, null, null,
                LocalDateTime.now(), List.of(), null);
    }

    private static RegisterRequest register(String password, AuthProvider provider) {
        return new RegisterRequest("Ana Popescu", "ana@exemplu.ro", password, 21,
                List.of(new SportLevelDto(1L, SportLevel.BEGINNER)), null, null, null, provider);
    }

    @Test
    void registerRejectsShortPasswordForLocalAccounts() {
        ApiException e = assertThrows(ApiException.class,
                () -> authService.register(register("12345", AuthProvider.LOCAL)));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
        assertEquals("VALIDATION_ERROR", e.getCode());
        assertTrue(e.getFields().containsKey("password"));
        verify(userService, never()).createUser(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userService.emailExists("ana@exemplu.ro")).thenReturn(true);
        ApiException e = assertThrows(ApiException.class,
                () -> authService.register(register("parola123", AuthProvider.LOCAL)));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
        assertEquals("EMAIL_TAKEN", e.getCode());
    }

    @Test
    void registerHashesPasswordAndReturnsValidToken() {
        when(userService.createUser(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(7L);

        AuthResponse res = authService.register(register("parola123", AuthProvider.LOCAL));

        var hash = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(userService).createUser(eq("Ana Popescu"), eq("ana@exemplu.ro"), hash.capture(), eq(21),
                any(), any(), any(), eq(AuthProvider.LOCAL), any());
        assertNotEquals("parola123", hash.getValue());
        assertTrue(encoder.matches("parola123", hash.getValue()));
        assertEquals(Optional.of(7L), jwtService.parse(res.token()));
    }

    @Test
    void registerWithGoogleNeedsNoPassword() {
        when(userService.createUser(any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(8L);

        authService.register(register(null, AuthProvider.GOOGLE));

        verify(userService).createUser(any(), any(), isNull(), any(), any(), any(), any(),
                eq(AuthProvider.GOOGLE), any());
    }

    @Test
    void loginWithCorrectPasswordSucceeds() {
        when(userService.findCredentials("ana"))
                .thenReturn(Optional.of(new UserCredentials(7L, encoder.encode("demo123"))));

        AuthResponse res = authService.login(new LoginRequest("ana", "demo123", false));

        assertEquals(Optional.of(7L), jwtService.parse(res.token()));
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() {
        when(userService.findCredentials("ana"))
                .thenReturn(Optional.of(new UserCredentials(7L, encoder.encode("demo123"))));

        ApiException e = assertThrows(ApiException.class,
                () -> authService.login(new LoginRequest("ana", "gresita", false)));
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
        assertEquals("INVALID_CREDENTIALS", e.getCode());
    }

    @Test
    void loginWithUnknownUserIsUnauthorized() {
        when(userService.findCredentials("nimeni")).thenReturn(Optional.empty());
        ApiException e = assertThrows(ApiException.class,
                () -> authService.login(new LoginRequest("nimeni", "x", null)));
        assertEquals("INVALID_CREDENTIALS", e.getCode());
    }

    @Test
    void socialAccountWithoutPasswordCannotLogInWithPassword() {
        when(userService.findCredentials("ana")).thenReturn(Optional.of(new UserCredentials(7L, null)));
        assertThrows(ApiException.class, () -> authService.login(new LoginRequest("ana", "orice", false)));
    }

    @Test
    void rememberMeExtendsSessionToThirtyDays() {
        when(userService.findCredentials("ana"))
                .thenReturn(Optional.of(new UserCredentials(7L, encoder.encode("demo123"))));

        AuthResponse shortSession = authService.login(new LoginRequest("ana", "demo123", false));
        AuthResponse longSession = authService.login(new LoginRequest("ana", "demo123", true));

        long days = Duration.between(shortSession.expiresAt(), longSession.expiresAt()).toDays();
        assertTrue(days >= 28 && days <= 30, "diferență de zile: " + days);
    }
}
