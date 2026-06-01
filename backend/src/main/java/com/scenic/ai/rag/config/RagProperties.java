package com.scenic.ai.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rag")
public record RagProperties(
        EmbeddingProvider embeddingProvider,
        int maxTopK,
        int maxTopN,
        double minKeywordScoreForFusion,
        double minVectorScoreForFusion
) {
    public RagProperties {
        if (embeddingProvider == null) {
            embeddingProvider = EmbeddingProvider.HASH;
        }
        if (maxTopK <= 0) {
            maxTopK = 50;
        }
        if (maxTopN <= 0) {
            maxTopN = 10;
        }
        if (minKeywordScoreForFusion <= 0) {
            minKeywordScoreForFusion = 3.0;
        }
        if (minVectorScoreForFusion <= 0) {
            minVectorScoreForFusion = 0.18;
        }
    }

    public boolean useSpringAiEmbedding() {
        return embeddingProvider == EmbeddingProvider.SPRING_AI;
    }

    public enum EmbeddingProvider {
        HASH,
        SPRING_AI
    }
}
