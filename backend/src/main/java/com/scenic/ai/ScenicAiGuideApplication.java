package com.scenic.ai;

import com.scenic.ai.rag.config.RagProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RagProperties.class)
public class ScenicAiGuideApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScenicAiGuideApplication.class, args);
    }
}
