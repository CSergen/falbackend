package com.reisfal.falbackend.controller;

import com.reisfal.falbackend.model.Fortune;
import com.reisfal.falbackend.model.User;
import com.reisfal.falbackend.model.enums.FortuneCategory;
import com.reisfal.falbackend.repository.FortuneRepository;
import com.reisfal.falbackend.repository.UserRepository;
import com.reisfal.falbackend.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class FortuneController {

    private final FortuneRepository fortuneRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    public FortuneController(FortuneRepository fortuneRepository,
                             UserRepository userRepository,
                             AiService aiService) {
        this.fortuneRepository = fortuneRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    // 🔑 Numara → Enum map
    private FortuneCategory mapCategoryFromNumber(int num) {
        return switch (num) {
            case 1 -> FortuneCategory.ASK;
            case 2 -> FortuneCategory.IS;
            case 3 -> FortuneCategory.HEALTH;
            case 4 -> FortuneCategory.SPIRIT;
            default -> FortuneCategory.GENERAL;
        };
    }

    // 📌 Fal yükleme
    @PostMapping("/fortune")
    public ResponseEntity<?> uploadFortune(
            @RequestParam("image") MultipartFile image,
            @RequestParam("category") int categoryNumber,
            Authentication authentication) throws Exception {

        FortuneCategory category = mapCategoryFromNumber(categoryNumber);

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String uniqueFileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        String uploadDir = "/app/uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        Path filePath = Paths.get(uploadDir, uniqueFileName);
        Files.write(filePath, image.getBytes());

        String fortuneText;
        try {
            fortuneText = aiService.analyzeImage(filePath.toString(), category.getDisplayName());
        } catch (Exception e) {
            fortuneText = "AI yorumu alınamadı.";
        }

        Fortune fortune = new Fortune();
        fortune.setUser(user);
        fortune.setImageUrl(uniqueFileName);
        fortune.setFortuneText(fortuneText);
        fortune.setCreatedAt(LocalDateTime.now().toString());

        fortuneRepository.save(fortune);

        return ResponseEntity.ok(fortune);
    }

    // 📌 Kullanıcının geçmiş fallarını listeleme
    @GetMapping("/fortunes")
    public ResponseEntity<?> getUserFortunes(Authentication authentication, HttpServletRequest request) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Fortune> fortunes = fortuneRepository.findByUser(user);

        String baseUrl = String.format("%s://%s:%d/uploads/",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort());

        fortunes.forEach(f -> f.setImageUrl(baseUrl + f.getImageUrl()));

        return ResponseEntity.ok(fortunes);
    }

    // 📌 Yüklenen görselleri döndürme
    @GetMapping("/uploads/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        Path file = Paths.get("/app/uploads").resolve(filename);
        Resource resource = new UrlResource(file.toUri());

        String contentType = Files.probeContentType(file);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
