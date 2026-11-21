package ro.utcn.sportbuddy.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ro.utcn.sportbuddy.auth.model.AuthToken;
import ro.utcn.sportbuddy.auth.model.AuthTokenType;

import java.time.Instant;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByTokenHash(String tokenHash);

    Optional<AuthToken> findTopByUserIdAndTypeOrderByCreatedAtDesc(Long userId, AuthTokenType type);

    // un link nou (sau unul folosit) le anulează pe cele trimise înainte
    @Modifying
    @Query("update AuthToken t set t.usedAt = :now where t.userId = :userId and t.type = :type and t.usedAt is null")
    void invalidateAll(Long userId, AuthTokenType type, Instant now);
}
