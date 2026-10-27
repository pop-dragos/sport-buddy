package ro.utcn.sportbuddy.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.utcn.sportbuddy.users.model.UserSettings;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
}
