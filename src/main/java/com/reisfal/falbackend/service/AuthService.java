package com.reisfal.falbackend.service;

import com.reisfal.falbackend.model.RefreshToken;
import com.reisfal.falbackend.model.User;
import com.reisfal.falbackend.model.dto.LoginResponse;
import com.reisfal.falbackend.repository.RefreshTokenRepository;
import com.reisfal.falbackend.repository.UserRepository;
import com.reisfal.falbackend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwt;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh.expiration:604800000}") // 7 gün
    private long refreshValidityMs;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtTokenProvider jwt,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwt = jwt;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent())
            throw new RuntimeException("Kullanıcı adı zaten kayıtlı");
        if (userRepository.findByEmail(email).isPresent())
            throw new RuntimeException("Email zaten kayıtlı");

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return "Kullanıcı başarıyla kaydedildi";
    }

    @Transactional
    public LoginResponse login(String identifier, String rawPassword, String device) {
        String id = identifier == null ? "" : identifier.trim();
        boolean looksLikeEmail = id.contains("@");

        User user;
        if (looksLikeEmail) {
            user = userRepository.findByEmailIgnoreCase(id)
                    .orElseThrow(() -> new RuntimeException("Email ile kullanıcı bulunamadı"));
        } else {
            user = userRepository.findByUsername(id)
                    .or(() -> userRepository.findByEmailIgnoreCase(id))
                    .orElseThrow(() -> new RuntimeException("Kullanıcı adı ile kullanıcı bulunamadı"));
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword()))
            throw new RuntimeException("Şifre yanlış");

        String access = jwt.createAccessToken(user.getUsername());
        String rawRefresh = UUID.randomUUID().toString() + "." + UUID.randomUUID();
        String refreshHash = hash(rawRefresh);

        refreshTokenRepository.deleteByUser(user);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(refreshHash);
        rt.setExpiresAt(Instant.now().plusMillis(refreshValidityMs));
        rt.setDevice(device);
        refreshTokenRepository.save(rt);

        return new LoginResponse(access, rawRefresh);
    }

    // REFRESH → DB doğrula, süresi geçmemişse rotate et, yeni ikiliyi dön
    public LoginResponse refresh(String rawRefreshToken, String device) {
        String hash = hash(rawRefreshToken);
        RefreshToken token = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash)
                .orElseThrow(() -> new RuntimeException("Refresh token bulunamadı"));

        if (token.getExpiresAt().isBefore(Instant.now()))
            throw new RuntimeException("Refresh token süresi dolmuş");

        // rotate: eskiyi revoke et
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);

        String newRaw = UUID.randomUUID().toString() + "." + UUID.randomUUID();
        String newHash = hash(newRaw);

        RefreshToken newToken = new RefreshToken();
        newToken.setUser(token.getUser());
        newToken.setTokenHash(newHash);
        newToken.setExpiresAt(Instant.now().plusMillis(refreshValidityMs));
        newToken.setDevice(device);
        refreshTokenRepository.save(newToken);

        String newAccess = jwt.createAccessToken(token.getUser().getUsername());
        return new LoginResponse(newAccess, newRaw);
    }

    public void logout(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
