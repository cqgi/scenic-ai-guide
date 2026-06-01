package com.scenic.ai.rag.service;

import com.scenic.ai.rag.config.RagProperties;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class SpringAiEmbeddingService implements EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final RagProperties ragProperties;
    private final HashEmbeddingService fallback;

    public SpringAiEmbeddingService(EmbeddingModel embeddingModel, RagProperties ragProperties, HashEmbeddingService fallback) {
        this.embeddingModel = embeddingModel;
        this.ragProperties = ragProperties;
        this.fallback = fallback;
    }

    @Override
    public double[] embed(String text) {
        if (!ragProperties.useSpringAiEmbedding()) {
            return fallback.embed(text);
        }
        try {
            return toDoubleArray(embeddingModel.embed(text));
        } catch (RuntimeException exception) {
            return fallback.embed(text);
        }
    }

    private double[] toDoubleArray(float[] values) {
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = values[i];
        }
        return result;
    }
}
