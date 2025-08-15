package com.reisfal.falbackend.controller;

import com.reisfal.falbackend.model.Fortune;
import com.reisfal.falbackend.model.User;
import com.reisfal.falbackend.model.dto.ImageRequest;
import com.reisfal.falbackend.model.enums.FortuneCategory;
import com.reisfal.falbackend.repository.FortuneRepository;
import com.reisfal.falbackend.repository.UserRepository;
import com.reisfal.falbackend.service.AiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class FortuneController {

    private final FortuneRepository fortuneRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    public FortuneController(FortuneRepository fortuneRepository,
                             UserRepository userRepository,
                             AiService aiService) {
        this.fortuneRepository = fortuneRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

    // === Kategori yardımcıları ===
    private FortuneCategory mapCategoryFromNumber(int num) {
        return switch (num) {
            case 1 -> FortuneCategory.KAHVE;
            case 2 -> FortuneCategory.EL;
            case 3 -> FortuneCategory.TAROT;
            case 4 -> FortuneCategory.ASTROLOJI;
            case 5 -> FortuneCategory.ASK;
            case 6 -> FortuneCategory.GENEL;
            default -> FortuneCategory.GENEL;
        };
    }

    private FortuneCategory parseCategoryParam(String raw) {
        if (raw == null || raw.isBlank()) return FortuneCategory.GENEL;
        try {
            int n = Integer.parseInt(raw.trim());
            return mapCategoryFromNumber(n);
        } catch (NumberFormatException ignore) {
            String v = raw.trim().toLowerCase(Locale.ROOT);
            return switch (v) {
                case "kahve" -> FortuneCategory.KAHVE;
                case "el" -> FortuneCategory.EL;
                case "tarot" -> FortuneCategory.TAROT;
                case "astroloji" -> FortuneCategory.ASTROLOJI;
                case "ask", "aşk" -> FortuneCategory.ASK;
                default -> FortuneCategory.GENEL;
            };
        }
    }

    // === Multipart upload (opsiyonel) ===
    @PostMapping("/fortune")
    public ResponseEntity<?> uploadFortune(@RequestParam("image") MultipartFile image,
                                           @RequestParam("category") int categoryNumber,
                                           Authentication authentication) throws Exception {

        FortuneCategory category = mapCategoryFromNumber(categoryNumber);

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String originalName = Optional.ofNullable(image.getOriginalFilename()).orElse("image.jpg"); // 👈 null guard
        String uniqueFileName = UUID.randomUUID() + "_" + originalName;

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        Path filePath = Paths.get(uploadDir, uniqueFileName);
        Files.write(filePath, image.getBytes());

        String fortuneText;
        try {
            byte[] imageBytes = Files.readAllBytes(filePath);
            fortuneText = aiService.analyzeImage(imageBytes, category.getDisplayName());
        } catch (Exception e) {
            fortuneText = "AI yorumu alınamadı.";
        }

        Fortune fortune = new Fortune();
        fortune.setUser(user);
        fortune.setCategory(category); // 👈 EKLENDİ
        fortune.setImageUrl(uniqueFileName);
        fortune.setFortuneText(fortuneText);
        fortune.setCreatedAt(LocalDateTime.now().toString());

        fortuneRepository.save(fortune);

        return ResponseEntity.ok(fortune);
    }

    // === Base64 upload (Flutter Web/Mobil) — zengin yanıt ===
    @PostMapping("/api/fal")
    public ResponseEntity<?> analyzeBase64Image(@RequestBody ImageRequest imageRequest,
                                                Authentication authentication) {
        try {
            String base64Image = imageRequest.getImageBase64();
            if (base64Image == null || base64Image.isEmpty()) {
                return ResponseEntity.badRequest().body("Base64 görsel boş.");
            }

            FortuneCategory category = parseCategoryParam(imageRequest.getCategory());

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            String fileName = user.getUsername() + "_" + UUID.randomUUID() + ".jpg";

            Path uploadBase = Paths.get(uploadDir);
            Files.createDirectories(uploadBase);
            Path filePath = uploadBase.resolve(fileName);
            Files.write(filePath, imageBytes);

            String fortuneText = aiService.analyzeImage(imageBytes, category.getDisplayName());

            Fortune fortune = new Fortune();
            fortune.setUser(user);
            fortune.setCategory(category); // 👈 EKLENDİ
            fortune.setImageUrl(fileName);
            fortune.setFortuneText(fortuneText);
            fortune.setCreatedAt(LocalDateTime.now().toString());
            fortuneRepository.save(fortune);

            String baseUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/uploads/")
                    .build()
                    .toUriString();

            Map<String, Object> body = new HashMap<>();
            body.put("comment", fortuneText);
            Map<String, Object> f = new HashMap<>();
            f.put("id", fortune.getId());
            f.put("imageUrl", baseUrl + fortune.getImageUrl());
            f.put("fortuneText", fortune.getFortuneText());
            f.put("createdAt", fortune.getCreatedAt());
            f.put("category", category.name()); // 👈 mevcut davranış korunuyor
            body.put("fortune", f);

            return ResponseEntity.ok(body);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Hata: " + e.getMessage());
        }
    }

    // === Kullanıcının geçmişi — DTO/MAP döndür (ENTITY'Yİ MUTATE ETME) ===
    @GetMapping("/fortunes")
    public ResponseEntity<?> getUserFortunes(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Fortune> fortunes = fortuneRepository.findByUser(user);

        String baseUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/uploads/")
                .build()
                .toUriString();

        List<Map<String, Object>> resp = fortunes.stream()
                .map(f -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", f.getId());
                    m.put("imageUrl", baseUrl + f.getImageUrl());
                    m.put("fortuneText", f.getFortuneText());
                    m.put("createdAt", f.getCreatedAt());
                    m.put("category", f.getCategory().name()); // 👈 EKLENDİ
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(resp);
    }

    // === Tek kaydı ve dosyayı sil (opsiyonel; frontend destekliyor) ===
    @DeleteMapping("/fortunes/{id}")
    public ResponseEntity<?> deleteFortune(@PathVariable Long id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Fortune f = fortuneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fortune not found"));

        if (!f.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        try {
            Files.deleteIfExists(Paths.get(uploadDir).resolve(f.getImageUrl()));
        } catch (Exception ignored) {}

        fortuneRepository.delete(f);
        return ResponseEntity.noContent().build();
    }

    // === Statik dosya servis (content-type null guard + 404) ===
    @GetMapping("/uploads/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        Path file = Paths.get(uploadDir).resolve(filename);
        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists() || !resource.isReadable()) { // 👈 404 KORUMA
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
