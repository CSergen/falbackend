package com.reisfal.falbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reisfal.falbackend.client.GeminiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class AiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public AiService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
        this.objectMapper = new ObjectMapper();
    }

    public String analyzeImage(String imagePath, String category) throws Exception {
        byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        String prompt = String.format(
                "Sen deneyimli bir kahve falcısısın. Fotoğrafı dikkatle incele ve yalnızca kahve falına uygun bir fincan veya telve varsa fal yorumu yap. " +
                        "Eğer fotoğraf kahve falına uygun değilse, net bir şekilde şu cevabı ver: 'Bu fotoğraf kahve falına uygun değil, fal yorumu yapılamaz.' " +
                        "Eğer kahve falına uygunsa, '%s' kategorisine odaklanarak güçlü, kendine güvenen, falcı tarzında bir yorum yap. " +
                        "Kesin ifadeler kullan, belirsizlikten bahsetme. 'Yakın zamanda...', 'Kaderinizde...', 'Büyük bir değişim...', 'Şans ve bereket geliyor...' gibi etkileyici, manipülatif bir dil kullan. " +
                        "Sadece '%s' kategorisinden bahset, diğer konulara girme.",
                category, category
        );

        String requestBody = """
        {
          "contents": [
            {
              "parts": [
                { "text": "%s" },
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

        String response = geminiClient.generateContent(geminiApiKey, requestBody);
        return extractTextFromGeminiResponse(response);
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
