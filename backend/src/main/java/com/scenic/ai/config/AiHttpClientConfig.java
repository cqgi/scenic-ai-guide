package com.scenic.ai.config;

import com.scenic.ai.ai.AiProperties;
import java.time.Duration;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Configuration
public class AiHttpClientConfig {

    @Bean
    RestClientCustomizer aiRestClientCustomizer(AiProperties aiProperties) {
        return builder -> builder.requestFactory(requestFactory(aiProperties.timeoutSeconds()));
    }

    private SimpleClientHttpRequestFactory requestFactory(int timeoutSeconds) {
        Duration timeout = Duration.ofSeconds(timeoutSeconds);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }
}
