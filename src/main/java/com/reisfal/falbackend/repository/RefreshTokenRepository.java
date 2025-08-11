package com.reisfal.falbackend.repository;

import com.reisfal.falbackend.model.RefreshToken;
import com.reisfal.falbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);
    void deleteByUser(User user);
    long deleteByExpiresAtBefore(Instant time);
}
