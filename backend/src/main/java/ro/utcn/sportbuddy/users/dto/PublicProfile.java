package ro.utcn.sportbuddy.users.dto;

import java.time.LocalDate;
import java.util.List;

public record PublicProfile(Long id, String username, String fullName, String avatarUrl,
                            Long cityId, String description, LocalDate memberSince,
                            List<SportLevelDto> sports) {}
