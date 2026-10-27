package ro.utcn.sportbuddy.users.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.utcn.sportbuddy.users.dto.UserSummary;
import ro.utcn.sportbuddy.users.service.UserService;

import java.util.List;

@RestController
@Profile("dev")
@RequestMapping("/api/users")
public class UserDevController {

    private final UserService userService;

    public UserDevController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserSummary> list() {
        return userService.findAll();
    }
}