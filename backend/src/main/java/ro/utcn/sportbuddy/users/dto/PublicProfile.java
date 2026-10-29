package ro.utcn.sportbuddy.users.dto;

import java.time.LocalDate;
import java.util.List;

// restricted = true: profilul e vizibil doar prietenilor, deci se trimit doar numele și poza.
public record PublicProfile(Long id, String username, String fullName, String avatarUrl,
                            Long cityId, String description, LocalDate memberSince,
                            List<SportLevelDto> sports, boolean restricted) {

    // forma de dinainte de câmpul restricted, folosită și de alte module (de exemplu în testele din ratings)
    public PublicProfile(Long id, String username, String fullName, String avatarUrl,
                         Long cityId, String description, LocalDate memberSince, List<SportLevelDto> sports) {
        this(id, username, fullName, avatarUrl, cityId, description, memberSince, sports, false);
    }
}
