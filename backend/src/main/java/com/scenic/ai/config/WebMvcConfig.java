package com.scenic.ai.config;

import com.scenic.ai.ai.AiProperties;
import java.nio.file.Path;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AiProperties aiProperties;

    public WebMvcConfig(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path audioDirectory = aiProperties.audioStoragePath().toAbsolutePath().normalize();
        String publicPath = aiProperties.audioPublicPath().replaceAll("/+$", "");
        registry.addResourceHandler(publicPath + "/**")
                .addResourceLocations(audioDirectory.toUri().toString());
    }
}
