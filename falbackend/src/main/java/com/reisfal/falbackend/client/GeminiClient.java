package com.reisfal.falbackend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "geminiClient", url = "https://generativelanguage.googleapis.com")
public interface GeminiClient {

    @PostMapping(
            value = "/v1beta/models/gemini-1.5-flash:generateContent",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    String generateContent(@RequestParam("key") String apiKey, @RequestBody String requestBody);
}
