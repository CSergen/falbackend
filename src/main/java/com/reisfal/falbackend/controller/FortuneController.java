package com.reisfal.falbackend.controller;

import com.reisfal.falbackend.model.Fortune;
import com.reisfal.falbackend.model.User;
<<<<<<< HEAD
=======
import com.reisfal.falbackend.model.dto.ImageRequest;
>>>>>>> recover-2157
import com.reisfal.falbackend.model.enums.FortuneCategory;
import com.reisfal.falbackend.repository.FortuneRepository;
import com.reisfal.falbackend.repository.UserRepository;
import com.reisfal.falbackend.service.AiService;
<<<<<<< HEAD
=======
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
>>>>>>> recover-2157
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
<<<<<<< HEAD
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
=======
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
>>>>>>> recover-2157

@RestController
public class FortuneController {

    private final FortuneRepository fortuneRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

<<<<<<< HEAD
=======
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

>>>>>>> recover-2157
    public FortuneController(FortuneRepository fortuneRepository,
                             UserRepository userRepository,
                             AiService aiService) {
        this.fortuneRepository = fortuneRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
    }

<<<<<<< HEAD
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
=======
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


    /** Multipart upload (mobil için kullanılabilir) */
>>>>>>> recover-2157
    @PostMapping("/fortune")
    public ResponseEntity<?> uploadFortune(
            @RequestParam("image") MultipartFile image,
            @RequestParam("category") int categoryNumber,
            Authentication authentication) throws Exception {

        FortuneCategory category = mapCategoryFromNumber(categoryNumber);

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

<<<<<<< HEAD
        String uniqueFileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        String uploadDir = "/app/uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

=======
        // upload klasörü
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String uniqueFileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
>>>>>>> recover-2157
        Path filePath = Paths.get(uploadDir, uniqueFileName);
        Files.write(filePath, image.getBytes());

        String fortuneText;
        try {
<<<<<<< HEAD
            fortuneText = aiService.analyzeImage(filePath.toString(), category.getDisplayName());
=======
            byte[] imageBytes = Files.readAllBytes(filePath);
            fortuneText = aiService.analyzeImage(imageBytes, category.getDisplayName());
>>>>>>> recover-2157
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

<<<<<<< HEAD
    // 📌 Kullanıcının geçmiş fallarını listeleme
=======
    /** Base64 görsel ile analiz (Flutter Web/Mobile ortak) */
    @PostMapping("/api/fal")
    public ResponseEntity<?> analyzeBase64Image(
            @RequestBody ImageRequest imageRequest,
            Authentication authentication
    ) {
        try {
            String base64Image = imageRequest.getImageBase64();
            String category = imageRequest.getCategory();

            if (base64Image == null || base64Image.isEmpty()) {
                return ResponseEntity.badRequest().body("Base64 görsel boş.");
            }
            if (category == null || category.isEmpty()) {
                category = "Genel";
            }

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // upload klasörü
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
            String fileName = user.getUsername() + "_" + System.currentTimeMillis() + ".jpg";
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, imageBytes);

            String fortuneText = aiService.analyzeImage(imageBytes, category);

            Fortune fortune = new Fortune();
            fortune.setUser(user);
            fortune.setImageUrl(fileName);
            fortune.setFortuneText(fortuneText);
            fortune.setCreatedAt(LocalDateTime.now().toString());
            fortuneRepository.save(fortune);

            return ResponseEntity.ok(Map.of("comment", fortuneText));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Hata: " + e.getMessage());
        }
    }

    /** Kullanıcının tüm fallarını listele (imageUrl'leri absolute yap) */
>>>>>>> recover-2157
    @GetMapping("/fortunes")
    public ResponseEntity<?> getUserFortunes(Authentication authentication, HttpServletRequest request) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Fortune> fortunes = fortuneRepository.findByUser(user);

<<<<<<< HEAD
        String baseUrl = String.format("%s://%s:%d/uploads/",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort());
=======
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .toUriString();
>>>>>>> recover-2157

        fortunes.forEach(f -> f.setImageUrl(baseUrl + f.getImageUrl()));

        return ResponseEntity.ok(fortunes);
    }

<<<<<<< HEAD
    // 📌 Yüklenen görselleri döndürme
    @GetMapping("/uploads/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        Path file = Paths.get("/app/uploads").resolve(filename);
        Resource resource = new UrlResource(file.toUri());

        String contentType = Files.probeContentType(file);
=======
    /** Upload edilmiş dosyayı servis et */
    @GetMapping("/uploads/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws MalformedURLException {
        Path file = Paths.get(uploadDir).resolve(filename);
        Resource resource = new UrlResource(file.toUri());

        String contentType = "application/octet-stream";
        try {
            String probed = Files.probeContentType(file);
            if (probed != null) contentType = probed;
        } catch (IOException ignored) {}
>>>>>>> recover-2157

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
