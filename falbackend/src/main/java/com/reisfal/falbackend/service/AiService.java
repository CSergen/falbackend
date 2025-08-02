package com.reisfal.falbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class AiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String analyzeImage(String imagePath, String category) throws Exception {
        byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        String prompt = String.format(
                "Sen deneyimli, yaşlı bilge ve bir o kadar da gizemli bir kahve falcısısın. Fotoğrafı dikkatle incele ve yalnızca kahve fincanı görseli varsa fal yorumu yap. " +
                        "Eğer fotoğraf kahve falına uygun değilse, net bir şekilde şu cevabı ver: 'Bu fotoğraf kahve falına uygun değil, fal yorumu yapılamaz.' " +
                        "Eğer kahve falına uygunsa, '%s' kategorisine odaklanarak güçlü, kendine güvenen, falcı tarzında bir yorum yap. " +
                        "Kullanacağın ifadeleri ve kelimeleri internetten kahve falına uygun şekillerde seç ve unutma ki her seferinde farklı bir fal bakman gerekiyor."+
                        "Kesin ifadeler kullan ama belirsiz ol geleceğe dair umut verici veya korkutucu tahminlerde bulun bu tahminler duruma göre yorumlanabilir olmalı 'Yakın zamanda...', 'Kaderinizde...', 'Büyük bir değişim...', 'Şans ve bereket geliyor...' gibi etkileyici, manipülatif bir dil kullan. " +
                        "Sadece '%s' kategorisinden bahset, diğer konulara girme.",
                category, category
        );




        String requestBody = """
        {
          "contents": [
            {
              "parts": [
                {
                  "text": "%s"
                },
                {
                  "inline_data": {
                    "mime_type": "image/png",
                    "data": "%s"
                  }
                }
              ]
            }
          ]
        }
        """.formatted(prompt, base64Image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey,
                HttpMethod.POST,
                entity,
                String.class
        );

        return extractTextFromGeminiResponse(response.getBody());
    }

    private String extractTextFromGeminiResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            return "AI yorumu alınamadı.";
        }
    }
}
