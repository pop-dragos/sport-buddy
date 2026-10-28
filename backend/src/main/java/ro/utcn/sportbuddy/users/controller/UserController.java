package ro.utcn.sportbuddy.users.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.users.dto.*;
import ro.utcn.sportbuddy.users.service.UserService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(CurrentUser.id(), req.currentPassword(), req.newPassword());
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
        return userService.getPublicProfile(id);
    }
}
