package ro.utcn.sportbuddy.users.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.users.dto.*;
import ro.utcn.sportbuddy.users.service.UserService;

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
