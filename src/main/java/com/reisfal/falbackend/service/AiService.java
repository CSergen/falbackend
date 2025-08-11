package com.reisfal.falbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reisfal.falbackend.client.GeminiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
=======
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
>>>>>>> recover-2157

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

<<<<<<< HEAD
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
=======
    /**
     * Base64 string -> byte[] -> analyze
     */
    public String analyzeBase64Image(String base64Image, String category) throws Exception {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        return analyzeImage(imageBytes, category);
    }

    /**
     * Ana çağrı: Görsel + kategoriye göre uygun prompt ile Gemini'yi çağırır.
     * category: "1..6" ya da "kahve/el/tarot/astroloji/ask/genel" gibi isimler kabul edilir.
     */
    public String analyzeImage(byte[] imageBytes, String category) throws Exception {
        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        String prompt = resolvePrompt(category);
        // JSON string içinde kaçışları güvene al
        String safePrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
>>>>>>> recover-2157

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
<<<<<<< HEAD
        """.formatted(prompt, base64Image);
=======
        """.formatted(safePrompt, base64);
>>>>>>> recover-2157

        String response = geminiClient.generateContent(geminiApiKey, requestBody);
        return extractTextFromGeminiResponse(response);
    }

<<<<<<< HEAD
    private String extractTextFromGeminiResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
=======
    /**
     * Gemini cevabından metni çeker.
     */
    private String extractTextFromGeminiResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode parts = root.path("candidates").get(0).path("content").path("parts");
            if (parts.isArray() && parts.size() > 0) {
                return parts.get(0).path("text").asText();
            }
            return "AI yorumu alınamadı.";
>>>>>>> recover-2157
        } catch (Exception e) {
            return "AI yorumu alınamadı.";
        }
    }
<<<<<<< HEAD
=======

    // -------------------- Prompt Seçici --------------------

    /**
     * 6 kategori desteği: Kahve, El, Tarot, Astroloji, Aşk, Genel (fotoğraf ne olursa olsun)
     * category "1..6" olabilir; ya da isim/etiket girilebilir (case-insensitive).
     */
    private String resolvePrompt(String categoryRaw) {
        // normalize
        String key = (categoryRaw == null ? "" : categoryRaw.trim().toLowerCase(Locale.ROOT));

        // sayısal ID -> isim
        switch (key) {
            case "1": key = "kahve"; break;
            case "2": key = "el"; break;
            case "3": key = "tarot"; break;
            case "4": key = "astroloji"; break;
            case "5": key = "ask"; break;   // aşk'ı normalize edeceğiz
            case "6": key = "genel"; break;
            default:  // isimle gelmişse normalize etmeye devam
                break;
        }

        // Türkçe karakterleri normalize et
        key = key.replace('ı','i').replace('ş','s').replace('ğ','g')
                .replace('ö','o').replace('ü','u').replace('ç','c');

        // --- SADECE TAM EŞLEŞME / güvenli eşleşme ---
        if (key.equals("kahve"))            key = "kahve";
        else if (key.equals("el"))          key = "el";
        else if (key.equals("tarot"))       key = "tarot";
        else if (key.equals("astroloji") || key.startsWith("astro")) key = "astroloji";
        else if (key.equals("ask"))         key = "ask";      // "aşk" -> "ask" oldu
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

>>>>>>> recover-2157
}
