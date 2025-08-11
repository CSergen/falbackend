package com.reisfal.falbackend.controller;

import com.reisfal.falbackend.model.User;
import com.reisfal.falbackend.model.dto.LoginRequest;
import com.reisfal.falbackend.model.dto.LoginResponse;
import com.reisfal.falbackend.model.dto.RefreshTokenRequest;
import com.reisfal.falbackend.model.dto.RegisterRequest;
import com.reisfal.falbackend.repository.UserRepository;
import com.reisfal.falbackend.security.JwtTokenProvider;
import com.reisfal.falbackend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections; // <-- ÖNEMLİ

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository; // <-- logout için gerekli
    private final JwtTokenProvider jwt;

    public AuthController(AuthService authService,
                          UserRepository userRepository,
                          JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.jwt = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String message = authService.register(
                    request.getUsername(), request.getEmail(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("message", message));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
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
    }
}
