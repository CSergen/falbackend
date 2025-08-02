package com.reisfal.falbackend.controller;

import com.reisfal.falbackend.model.dto.LoginRequest;
import com.reisfal.falbackend.model.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AliasAuthController {

    private final AuthController authController;

    public AliasAuthController(AuthController authController) {
        this.authController = authController;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAlias(@RequestBody RegisterRequest request) {
        return authController.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginAlias(@RequestBody LoginRequest request) {
        return authController.login(request);
    }
}
