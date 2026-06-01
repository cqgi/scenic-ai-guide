package com.scenic.ai.rag.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.scenic.ai.rag.config.RagProperties;
import com.scenic.ai.rag.model.RetrievalQuery;
import com.scenic.ai.rag.model.RetrievalResult;
import com.scenic.ai.rag.service.HybridRetrievalService;
import java.util.List;
import org.junit.jupiter.api.Test;

class RagDebugControllerTests {

    @Test
    void clampsRequestedTopKAndTopN() {
        CapturingHybridRetrievalService retrievalService = new CapturingHybridRetrievalService();
        RagDebugController controller = new RagDebugController(
                retrievalService,
                new RagProperties(RagProperties.EmbeddingProvider.HASH, 20, 8, 3.0, 0.18)
        );

        controller.search(new RagDebugController.RagSearchRequest("红叶谷", List.of(), 500, 100));

        assertThat(retrievalService.captured.topK()).isEqualTo(20);
        assertThat(retrievalService.captured.topN()).isEqualTo(8);
    }

    private static class CapturingHybridRetrievalService extends HybridRetrievalService {
        private RetrievalQuery captured;

        CapturingHybridRetrievalService() {
            super(null, null, null, null, null, null, null);
        }

        @Override
        public RetrievalResult search(RetrievalQuery query) {
            this.captured = query;
            return new RetrievalResult(query.query(), List.of(), List.of(), List.of(), List.of(), false, null);
        }
    }
}
