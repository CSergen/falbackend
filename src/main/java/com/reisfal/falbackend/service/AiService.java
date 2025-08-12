package com.reisfal.falbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reisfal.falbackend.client.GeminiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Locale;
import java.util.Map;

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

    /**
     * Base64 → byte[] → analyze
     */
    public String analyzeBase64Image(String base64Image, String category) throws Exception {
        if (base64Image == null || base64Image.isBlank()) {
            throw new IllegalArgumentException("Boş base64 görsel");
        }
        // data URL geldiyse ayıkla
        String clean = base64Image.contains(",")
                ? base64Image.substring(base64Image.indexOf(',') + 1)
                : base64Image;

        byte[] imageBytes = Base64.getDecoder().decode(clean);
        return analyzeImage(imageBytes, category);
    }

    /**
     * Ana çağrı: Görsel + kategoriye göre uygun prompt ile Gemini'yi çağırır.
     * category: "1..6" ya da "kahve/el/tarot/astroloji/ask/genel" kabul edilir.
     */
    public String analyzeImage(byte[] imageBytes, String category) throws Exception {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Boş görsel");
        }

        String mime = detectMimeType(imageBytes);
        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        String prompt = resolvePrompt(category);
        String safePrompt = escapeForJson(prompt);

        String requestBody = """
        {
          "contents": [
            {
              "parts": [
                { "text": "%s" },
                {
                  "inline_data": {
                    "mime_type": "%s",
                    "data": "%s"
                  }
                }
              ]
            }
          ]
        }
        """.formatted(safePrompt, mime, base64);

        String response = geminiClient.generateContent(geminiApiKey, requestBody);
        return extractTextFromGeminiResponse(response);
    }

    /**
     * Gemini cevabından metni çeker; boşsa anlamlı fallback döner.
     */
    private String extractTextFromGeminiResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);

            // Hata mesajı varsa önce onu kontrol et
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                String msg = error.path("message").asText("");
                if (!msg.isBlank()) {
                    return "AI hatası: " + msg;
                }
            }

            // Normal yol
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText("");
                    if (!text.isBlank()) return text;
                }
            }
            return "AI yorumu alınamadı.";
        } catch (Exception e) {
            return "AI yorumu alınamadı.";
        }
    }

    /**
     * Minimal, hızlı MIME tespiti (JPEG/PNG). Bilinmiyorsa jpeg’e düşer.
     */
    private String detectMimeType(byte[] bytes) {
        // JPEG: FF D8 FF
        if (bytes.length > 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (bytes.length > 8
                && (bytes[0] & 0xFF) == 0x89
                && (bytes[1] & 0xFF) == 0x50
                && (bytes[2] & 0xFF) == 0x4E
                && (bytes[3] & 0xFF) == 0x47) {
            return "image/png";
        }
        // (İstersen WEBP / GIF desteği de eklenir.)
        return "image/jpeg";
    }

    /**
     * JSON string içinde güvenli kaçış.
     */
    private String escapeForJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // -------------------- Prompt Seçici --------------------
    private String resolvePrompt(String categoryRaw) {
        String key = (categoryRaw == null ? "" : categoryRaw.trim().toLowerCase(Locale.ROOT));

        // sayısal ID -> isim
        switch (key) {
            case "1": key = "kahve"; break;
            case "2": key = "el"; break;
            case "3": key = "tarot"; break;
            case "4": key = "astroloji"; break;
            case "5": key = "ask"; break;      // "aşk" normalize ediliyor
            case "6": key = "genel"; break;
            default:  // isim gelmişse devam
        }

        // Türkçe karakter normalizasyonu
        key = key.replace('ı','i').replace('ş','s').replace('ğ','g')
                .replace('ö','o').replace('ü','u').replace('ç','c');

        if (key.equals("kahve"))            key = "kahve";
        else if (key.equals("el"))          key = "el";
        else if (key.equals("tarot"))       key = "tarot";
        else if (key.equals("astroloji") || key.startsWith("astro")) key = "astroloji";
        else if (key.equals("ask"))         key = "ask";
        else if (key.equals("genel") || key.startsWith("gen")) key = "genel";
        else key = "genel"; // fallback

        Map<String, String> PROMPTS = Map.of(
                "kahve",
                "Sen tecrübeli bir kahve falı yorumcususun. Kullanıcının yüklediği kahve fincanı fotoğrafındaki telve şekillerini, izleri ve sembolleri analiz et. Fal yorumunu olumlu-olumsuz dengeli tut; aşk, iş/kariyer, sağlık ve genel yaşam konularına değin. Geleceğe yönelik sezgisel öngörüler ekle. Samimi ve akıcı bir üslup kullan; en az 3-4 paragraf yaz.",
                "el",
                "Sen uzman bir el falcısısın. Fotoğraftaki avuç içi çizgilerini (yaşam, kalp, akıl çizgisi vb.), tepecikleri ve parmak oranlarını sembolik olarak yorumla. Karakter, aşk ilişkileri, sağlık ve kariyer hakkında içgörüler ver. Umut veren ama gerçekçi bir ton kullan; en az 3-4 paragraf uzunluğunda yaz.",
                "tarot",
                "Sen tecrübeli bir tarot yorumcususun. Görseldeki kart(lar)ın isimlerini ve sembollerini açıklayarak aralarındaki ilişkiyi yorumla. Aşk, iş/kariyer, sağlık ve ruhsal gelişim başlıklarında derin ve mistik bir anlatı kur. En az 3-4 paragraf halinde, akıcı ve anlaşılır bir dille yaz.",
                "astroloji",
                "Sen deneyimli bir astrologsun. Görseldeki astrolojik sembolleri, burç/gezegen/ev çağrışımlarını referans alarak kullanıcının genel eğilimleri, fırsatları ve dikkat etmesi gereken temalar hakkında yorum yap. Aşk, kariyer ve sağlığa değin; en az 3-4 paragraf yaz, umut verici bir üslup kullan.",
                "ask",
                "Sen uzman bir aşk falı yorumcususun. Fotoğraftan aldığın sembolik ipuçlarıyla kişinin aşk hayatına, ilişkilerine ve duygusal gündemine dair içgörüler sun. Yakın geleceğe yönelik olası senaryoları anlat; iletişim, uyum ve karmik dersler gibi temalara değin. En az 3-4 paragraf yaz; sıcak ve empatik ol.",
                "genel",
                "Sen yaratıcı ve sezgisel bir yorumcususun. Kullanıcının yüklediği herhangi bir fotoğrafı (konu fark etmeksizin) renkler, kompozisyon ve hisler üzerinden yorumla. Bu görselden yola çıkarak kişinin ruh hali, yaşam döngüsü ve geleceğine dair sembolik öngörüler üret. Aşk, iş/kariyer ve sağlık başlıklarına değin; en az 3-4 paragraflık, akıcı ve mistik bir anlatım kullan."
        );

        return PROMPTS.getOrDefault(key, PROMPTS.get("genel"));
    }
}
