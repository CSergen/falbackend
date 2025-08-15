package com.reisfal.falbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var location = "file:" + (uploadDir.endsWith("/") ? uploadDir : uploadDir + "/");
        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
