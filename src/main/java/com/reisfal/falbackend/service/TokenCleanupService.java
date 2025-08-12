package com.reisfal.falbackend.service;

import com.reisfal.falbackend.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;

@EnableScheduling
@Service
public class TokenCleanupService {
    private final RefreshTokenRepository repo;
    public TokenCleanupService(RefreshTokenRepository repo) { this.repo = repo; }

    @Scheduled(cron = "0 0 3 * * *") // her gece 03:00
    public void cleanupExpired() {
        long deleted = repo.deleteByExpiresAtBefore(Instant.now());
        System.out.println("🧹 expired refresh tokens deleted: " + deleted);
    }
}

