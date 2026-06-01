package com.scenic.ai.ai;

import com.scenic.ai.rag.service.EmbeddingService;
import org.springframework.stereotype.Service;

@Service
public class RagEmbeddingClient implements EmbeddingClient {

    private final EmbeddingService embeddingService;

    public RagEmbeddingClient(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @Override
    public double[] embed(String text) {
        return embeddingService.embed(text);
    }
}
