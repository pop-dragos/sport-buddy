package ro.utcn.sportbuddy.users.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.common.security.JwtService;
import ro.utcn.sportbuddy.users.dto.*;
import ro.utcn.sportbuddy.users.service.UserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/me")
    public UserMe me() {
        return userService.getMe(CurrentUser.id());
    }

    @PutMapping("/me")
    public UserMe updateMe(@Valid @RequestBody UpdateProfileRequest req) {
        return userService.updateMe(CurrentUser.id(), req);
    }

    // Lista se validează în service (minim un sport, sporturi existente), nu cu adnotări.
    @PutMapping("/me/sports")
    public UserMe updateSports(@RequestBody List<SportLevelDto> sports) {
        return userService.updateSports(CurrentUser.id(), sports);
    }

    // Celelalte dispozitive sunt deconectate; acesta primește un token nou, cu aceeași expirare ca cel vechi.
    // token = null când cererea nu a venit cu token (X-Dev-User în profilul dev).
    @PutMapping("/me/password")
    public Map<String, String> changePassword(@Valid @RequestBody ChangePasswordRequest req,
                                              @RequestHeader(value = "Authorization", required = false) String auth) {
        Long id = CurrentUser.id();
        int version = userService.changePassword(id, req.currentPassword(), req.newPassword());
        Map<String, String> res = new HashMap<>();
        res.put("token", null);
        if (auth != null && auth.startsWith("Bearer ")) {
            jwtService.parseSession(auth.substring(7))
                    .ifPresent(s -> res.put("token", jwtService.generate(id, version, s.expiresAt())));
        }
        return res;
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe() {
        userService.deleteMe(CurrentUser.id());
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserMe uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        return userService.updateAvatar(CurrentUser.id(), file.getBytes());
    }

    @DeleteMapping("/me/avatar")
    public UserMe deleteAvatar() {
        return userService.removeAvatar(CurrentUser.id());
    }

    @GetMapping("/me/settings")
    public UserSettingsDto settings() {
        return userService.getSettings(CurrentUser.id());
    }

    @PutMapping("/me/settings")
    public UserSettingsDto updateSettings(@Valid @RequestBody UserSettingsDto dto) {
        return userService.updateSettings(CurrentUser.id(), dto);
    }

    @GetMapping("/{id}")
    public PublicProfile profile(@PathVariable Long id) {
        return userService.getPublicProfile(CurrentUser.id(), id);
    }
}
