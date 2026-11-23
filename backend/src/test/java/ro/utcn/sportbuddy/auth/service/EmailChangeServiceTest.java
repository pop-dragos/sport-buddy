package ro.utcn.sportbuddy.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ro.utcn.sportbuddy.auth.dto.ChangeEmailRequest;
import ro.utcn.sportbuddy.auth.dto.TokenRequest;
import ro.utcn.sportbuddy.auth.model.AuthTokenType;
import ro.utcn.sportbuddy.common.error.ApiException;
import ro.utcn.sportbuddy.common.mail.MailService;
import ro.utcn.sportbuddy.users.dto.AuthAccount;
import ro.utcn.sportbuddy.users.dto.UserMe;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class EmailChangeServiceTest {

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private static final String HASH = ENCODER.encode("parola123");

    private UserService users;
    private AuthTokenService tokens;
    private MailService mail;
    private EmailChangeService service;
    private String pending;

    @BeforeEach
    void setUp() {
        users = mock(UserService.class);
        tokens = mock(AuthTokenService.class);
        mail = mock(MailService.class);
        service = new EmailChangeService(users, tokens, mail, ENCODER, "http://localhost:5173");
        pending = null;
        account(HASH);
        when(users.getMe(7L)).thenAnswer(i -> me());
        doAnswer(i -> { pending = i.getArgument(1); return null; }).when(users).setPendingEmail(eq(7L), any());
        when(tokens.create(any(), any(), any())).thenReturn("secret");
    }

    private void account(String hash) {
        when(users.findAccount(7L)).thenReturn(Optional.of(
                new AuthAccount(7L, "Ana Popescu", "ana@demo.ro", hash, true, null, 0)));
    }

    private UserMe me() {
        return new UserMe(7L, "ana", "Ana Popescu", "ana@demo.ro", null, 21, null, null, null,
                LocalDateTime.now(), List.of(), null, true, pending);
    }

    @Test
    void requestSendsLinkToNewAddressAndWarningToOldOne() {
        UserMe res = service.request(7L, new ChangeEmailRequest(" ana.noua@demo.ro ", "parola123"));

        assertEquals("ana.noua@demo.ro", res.pendingEmail());
        verify(tokens).create(eq(7L), eq(AuthTokenType.CHANGE_EMAIL), any());
        verify(mail).sendLink(eq("ana.noua@demo.ro"), any(), any(), any(), any(),
                eq("http://localhost:5173/confirm-email?token=secret"), any());
        verify(mail).sendNotice(eq("ana@demo.ro"), any(), any(), contains("ana.noua@demo.ro"), any());
    }

    @Test
    void requestNeedsTheCurrentPassword() {
        ApiException e = assertThrows(ApiException.class,
                () -> service.request(7L, new ChangeEmailRequest("ana.noua@demo.ro", "gresita")));

        assertEquals("WRONG_PASSWORD", e.getCode());
        assertTrue(e.getFields().containsKey("currentPassword"));
        assertNull(pending);
        verifyNoInteractions(mail);
    }

    @Test
    void googleAccountWithoutPasswordConfirmsOnlyByLink() {
        account(null);

        service.request(7L, new ChangeEmailRequest("ana.noua@demo.ro", null));

        assertEquals("ana.noua@demo.ro", pending);
    }

    @Test
    void requestRejectsSameOrTakenEmail() {
        assertThrows(ApiException.class, () -> service.request(7L, new ChangeEmailRequest("ANA@demo.ro", "parola123")));

        when(users.emailExists("bogdan@demo.ro")).thenReturn(true);
        ApiException e = assertThrows(ApiException.class,
                () -> service.request(7L, new ChangeEmailRequest("bogdan@demo.ro", "parola123")));
        assertEquals("EMAIL_TAKEN", e.getCode());
        verifyNoInteractions(mail);
    }

    @Test
    void sameRequestTwiceInAMinuteSendsOnlyOnce() {
        service.request(7L, new ChangeEmailRequest("ana.noua@demo.ro", "parola123"));
        when(tokens.sentRecently(eq(7L), eq(AuthTokenType.CHANGE_EMAIL), any())).thenReturn(true);

        service.request(7L, new ChangeEmailRequest("ana.noua@demo.ro", "parola123"));

        verify(tokens, times(1)).create(any(), any(), any());
    }

    @Test
    void cancelClearsPendingAndKillsLinks() {
        pending = "ana.noua@demo.ro";

        UserMe res = service.cancel(7L);

        assertNull(res.pendingEmail());
        verify(tokens).invalidate(7L, AuthTokenType.CHANGE_EMAIL);
    }

    @Test
    void confirmAppliesThePendingEmail() {
        when(tokens.consume("secret", AuthTokenType.CHANGE_EMAIL)).thenReturn(Optional.of(7L));
        when(users.applyPendingEmail(7L)).thenReturn("ana.noua@demo.ro");

        assertEquals("ana.noua@demo.ro", service.confirm(new TokenRequest("secret")));
    }

    @Test
    void confirmWithBadLinkFails() {
        when(tokens.consume("secret", AuthTokenType.CHANGE_EMAIL)).thenReturn(Optional.empty());

        ApiException e = assertThrows(ApiException.class, () -> service.confirm(new TokenRequest("secret")));

        assertEquals("INVALID_LINK", e.getCode());
        verify(users, never()).applyPendingEmail(any());
    }

    @Test
    void resendWithoutPendingFails() {
        assertThrows(ApiException.class, () -> service.resend(7L));
    }
}
