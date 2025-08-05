package com.reisfal.falbackend.service;

import com.reisfal.falbackend.model.User;
import com.reisfal.falbackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Kullanıcı adı zaten kayıtlı");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email zaten kayıtlı");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        return "Kullanıcı başarıyla kaydedildi";
    }

    public User login(String identifier, String password) {
        User user = userRepository.findByUsername(identifier)
                .orElse(userRepository.findByEmail(identifier)
                        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı")));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Şifre yanlış");
        }

        return user;
    }
}
