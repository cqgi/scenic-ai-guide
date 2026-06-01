package com.scenic.ai.rag.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HashEmbeddingServiceTests {

    private final HashEmbeddingService service = new HashEmbeddingService();

    @Test
    void embedReturnsNormalized1536DimensionalVector() {
        double[] vector = service.embed("红叶谷适合拍照");

        double norm = 0;
        for (double value : vector) {
            norm += value * value;
        }
        assertThat(vector).hasSize(1536);
        assertThat(Math.sqrt(norm)).isBetween(0.999, 1.001);
    }

    @Test
    void embedIsDeterministic() {
        assertThat(service.embed("古寺遗址")).containsExactly(service.embed("古寺遗址"));
    }
}
