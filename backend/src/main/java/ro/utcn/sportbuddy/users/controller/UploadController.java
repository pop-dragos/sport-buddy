package ro.utcn.sportbuddy.users.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ro.utcn.sportbuddy.users.service.UserService;

import java.time.Duration;

// Public (fără token), pentru că <img> din browser nu poate trimite antetul Authorization.
@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UserService userService;

    public UploadController(UserService userService) {
        this.userService = userService;
    }

    // Numele conține un uuid nou la fiecare încărcare, deci poza se poate ține în cache mult timp.
    @GetMapping("/{name}")
    public ResponseEntity<Resource> avatar(@PathVariable String name) {
        return userService.findAvatarFile(name)
                .map(path -> {
                    Resource file = new FileSystemResource(path);
                    MediaType type = MediaTypeFactory.getMediaType(file).orElse(MediaType.APPLICATION_OCTET_STREAM);
                    return ResponseEntity.ok()
                            .contentType(type)
                            .cacheControl(CacheControl.maxAge(Duration.ofDays(30)))
                            .body(file);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
