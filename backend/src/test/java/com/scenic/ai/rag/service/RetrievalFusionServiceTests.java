package com.scenic.ai.rag.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.scenic.ai.rag.config.RagProperties;
import com.scenic.ai.rag.model.RetrievalCandidate;
import com.scenic.ai.rag.model.RetrievalQuery;
import java.util.List;
import org.junit.jupiter.api.Test;

class RetrievalFusionServiceTests {

    private final RetrievalFusionService service = new RetrievalFusionService(
            new RagProperties(RagProperties.EmbeddingProvider.HASH, 50, 10, 3.0, 0.18)
    );

    @Test
    void ignoresWeakKeywordAndVectorScoresDuringFusion() {
        RetrievalCandidate weakKeyword = candidate(1L, "keyword");
        weakKeyword.setKeywordScore(2.0);
        RetrievalCandidate weakVector = candidate(2L, "vector");
        weakVector.setVectorScore(0.1);

        List<RetrievalCandidate> results = service.fuse(
                new RetrievalQuery("明镜湖亲水步道", List.of(), 10, 5),
                List.of(),
                List.of(weakKeyword),
                List.of(weakVector)
        );

        assertThat(results).extracting(RetrievalCandidate::getFinalScore).containsExactly(0.0, 0.0);
    }

    @Test
    void keepsStrongKeywordAndVectorScores() {
        RetrievalCandidate keyword = candidate(1L, "keyword");
        keyword.setKeywordScore(5.0);
        RetrievalCandidate vector = candidate(2L, "vector");
        vector.setVectorScore(0.3);

        List<RetrievalCandidate> results = service.fuse(
                new RetrievalQuery("明镜湖亲水步道", List.of(), 10, 5),
                List.of(),
                List.of(keyword),
                List.of(vector)
        );

        assertThat(results.get(0).getFinalScore()).isGreaterThan(0);
        assertThat(results.get(1).getFinalScore()).isGreaterThan(0);
    }

    private RetrievalCandidate candidate(Long id, String source) {
        RetrievalCandidate candidate = new RetrievalCandidate();
        candidate.setChunkId(id);
        candidate.setDocumentId(1L);
        candidate.setTitle("测试标题");
        candidate.setContent("测试内容");
        candidate.setSourceType(source);
        return candidate;
    }
}
