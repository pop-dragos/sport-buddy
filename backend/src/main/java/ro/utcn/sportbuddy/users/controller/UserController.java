package ro.utcn.sportbuddy.users.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.utcn.sportbuddy.common.security.CurrentUser;
import ro.utcn.sportbuddy.users.dto.UserMe;
import ro.utcn.sportbuddy.users.service.UserService;

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
}