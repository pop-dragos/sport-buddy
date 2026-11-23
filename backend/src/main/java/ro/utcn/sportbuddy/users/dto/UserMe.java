package ro.utcn.sportbuddy.users.dto;

import java.time.LocalDateTime;
import java.util.List;

// hasPassword: false la conturile create cu Google, care își pot seta o parolă prin email.
// pendingEmail: emailul nou cerut din Setări, care așteaptă confirmarea (null dacă nu e niciunul).
public record UserMe(Long id, String username, String fullName, String email, String phone,
                     Integer age, String avatarUrl, Long cityId, String description,
                     LocalDateTime createdAt, List<SportLevelDto> sports,
                     ProfileCompletion profileCompletion, boolean hasPassword, String pendingEmail) {

    public UserMe(Long id, String username, String fullName, String email, String phone,
                  Integer age, String avatarUrl, Long cityId, String description,
                  LocalDateTime createdAt, List<SportLevelDto> sports, ProfileCompletion profileCompletion) {
        this(id, username, fullName, email, phone, age, avatarUrl, cityId, description, createdAt, sports,
                profileCompletion, true, null);
    }
}
