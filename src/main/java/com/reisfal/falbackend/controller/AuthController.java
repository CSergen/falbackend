package com.reisfal.falbackend.controller;

import com.reisfal.falbackend.model.User;
import com.reisfal.falbackend.model.dto.LoginRequest;
<<<<<<< HEAD
import com.reisfal.falbackend.model.dto.RegisterRequest;
import com.reisfal.falbackend.model.dto.RefreshTokenRequest;
=======
import com.reisfal.falbackend.model.dto.LoginResponse;
import com.reisfal.falbackend.model.dto.RefreshTokenRequest;
import com.reisfal.falbackend.model.dto.RegisterRequest;
import com.reisfal.falbackend.repository.UserRepository;
>>>>>>> recover-2157
import com.reisfal.falbackend.security.JwtTokenProvider;
import com.reisfal.falbackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

<<<<<<< HEAD
import java.util.HashMap;
import java.util.Map;
=======
import java.util.Collections; // <-- ÖNEMLİ
>>>>>>> recover-2157

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
<<<<<<< HEAD
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
=======
    private final UserRepository userRepository; // <-- logout için gerekli
    private final JwtTokenProvider jwt;

    public AuthController(AuthService authService,
                          UserRepository userRepository,
                          JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.jwt = jwtTokenProvider;
>>>>>>> recover-2157
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String message = authService.register(
<<<<<<< HEAD
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword()
            );
            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
=======
                    request.getUsername(), request.getEmail(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("error", e.getMessage()));
>>>>>>> recover-2157
        }
    }

    @PostMapping("/login")
<<<<<<< HEAD
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // ✅ Username veya Email ile giriş yapabilmek için
            String identifier = (request.getUsername() != null && !request.getUsername().isEmpty())
                    ? request.getUsername()
                    : request.getEmail();

            User user = authService.login(identifier, request.getPassword());

            String accessToken = jwtTokenProvider.createAccessToken(user.getUsername());
            String refreshToken = jwtTokenProvider.createRefreshToken(user.getUsername());

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Geçersiz refresh token"));
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(username);

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(Map.of("username", authentication.getName()));
=======
    public ResponseEntity<?> login(@RequestBody LoginRequest req,
                                   @RequestHeader(value = "User-Agent", required = false) String ua) {
        try {
            // AuthService.login -> LoginResponse döner
            LoginResponse res = authService.login(req.getIdentifier(), req.getPassword(), ua);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest req,
                                     @RequestHeader(value = "User-Agent", required = false) String ua) {
        try {
            // refresh -> rotate + yeni access/refresh
            LoginResponse res = authService.refresh(req.getRefreshToken(), ua);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication != null) {
            String username = authentication.getName();
            User user = userRepository.findByUsernameOrEmail(username, username).orElse(null);
            if (user != null) {
                authService.logout(user);
            }
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authn) {
        if (authn == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Unauthorized"));
        }
        return ResponseEntity.ok(Collections.singletonMap("username", authn.getName()));
>>>>>>> recover-2157
    }
}
