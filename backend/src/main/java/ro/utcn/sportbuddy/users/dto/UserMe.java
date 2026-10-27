package ro.utcn.sportbuddy.users.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserMe(Long id, String username, String fullName, String email, String phone,
                     Integer age, String avatarUrl, Long cityId, String description,
                     LocalDateTime createdAt, List<SportLevelDto> sports,
                     ProfileCompletion profileCompletion) {}