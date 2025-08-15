package com.reisfal.falbackend.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class CorsConfig {

    // application*.properties veya .env üzerinden gelir
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();

        // Virgülle ayrılmış listeden besle
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim).toList());
        } else {
            config.addAllowedOriginPattern("*");
        }

        config.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization","Content-Type","Accept","Origin"));
        config.setAllowCredentials(false); // Token header'ı ile çalıştığımız için false yeterli

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
