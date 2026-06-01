package com.scenic.ai.rag.service;

import com.scenic.ai.rag.model.KnowledgeChunk;
import com.scenic.ai.rag.model.RetrievalCandidate;
import com.scenic.ai.rag.model.RetrievalQuery;
import com.scenic.ai.rag.repository.KnowledgeChunkRepository;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class VectorRetrievalService {

    private final EmbeddingService embeddingService;
    private final PgVectorFormatter pgVectorFormatter;
    private final EntityManager entityManager;
    private final KnowledgeChunkRepository knowledgeChunkRepository;

    public VectorRetrievalService(
            EmbeddingService embeddingService,
            PgVectorFormatter pgVectorFormatter,
            EntityManager entityManager,
            KnowledgeChunkRepository knowledgeChunkRepository
    ) {
        this.embeddingService = embeddingService;
        this.pgVectorFormatter = pgVectorFormatter;
        this.entityManager = entityManager;
        this.knowledgeChunkRepository = knowledgeChunkRepository;
    }

    public List<RetrievalCandidate> retrieve(RetrievalQuery query) {
        String vector = pgVectorFormatter.format(embeddingService.embed(query.query()));
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                        SELECT id, 1 - (embedding <=> CAST(:embedding AS vector)) AS score
                        FROM knowledge_chunk
                        WHERE enabled = true
                          AND embedding IS NOT NULL
                        ORDER BY embedding <=> CAST(:embedding AS vector)
                        LIMIT :limit
                        """)
                .setParameter("embedding", vector)
                .setParameter("limit", query.topK())
                .getResultList();

        Map<Long, Double> scores = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Number id = (Number) row[0];
            Number score = (Number) row[1];
            scores.put(id.longValue(), score.doubleValue());
        }
        Map<Long, KnowledgeChunk> chunks = new LinkedHashMap<>();
        for (KnowledgeChunk chunk : knowledgeChunkRepository.findByIdIn(scores.keySet())) {
            chunks.put(chunk.getId(), chunk);
        }

        List<RetrievalCandidate> candidates = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : scores.entrySet()) {
            KnowledgeChunk chunk = chunks.get(entry.getKey());
            if (chunk == null) {
                continue;
            }
            RetrievalCandidate candidate = RetrievalCandidate.fromChunk(chunk, "vector");
            candidate.setVectorScore(Math.max(entry.getValue(), 0));
            candidate.addHitReason("vector_cosine");
            candidates.add(candidate);
        }
        return candidates;
    }
}
