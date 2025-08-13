package com.reisfal.falbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // Domain yoksa sadece IP ve local geliştirme için açıyoruz
                        .allowedOriginPatterns(
                                "http://localhost:*",
                                "http://127.0.0.1:*",
                                "http://10.0.2.2:*",   // Android emulator
                                "http://192.168.*:*",  // Aynı Wi‑Fi’daki cihazlar
                                "http://13.60.172.6",        // <- EC2 IP'n (80/443)
                                "http://13.60.172.6:8080"    // <- 8080'dan erişiyorsan
                        )
                        .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization","Content-Type")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
