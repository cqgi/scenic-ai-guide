package com.scenic.ai.rag.service;

import com.scenic.ai.rag.model.KnowledgeChunk;
import com.scenic.ai.rag.model.RetrievalCandidate;
import com.scenic.ai.rag.model.RetrievalQuery;
import com.scenic.ai.rag.repository.KnowledgeChunkRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class Bm25RetrievalService {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final TextProcessingService textProcessingService;

    public Bm25RetrievalService(KnowledgeChunkRepository knowledgeChunkRepository, TextProcessingService textProcessingService) {
        this.knowledgeChunkRepository = knowledgeChunkRepository;
        this.textProcessingService = textProcessingService;
    }

    public List<RetrievalCandidate> retrieve(RetrievalQuery query) {
        String fullTextQuery = buildFullTextQuery(query.query());
        if (fullTextQuery.isBlank()) {
            return List.of();
        }
        List<RetrievalCandidate> candidates = new ArrayList<>();
        List<KnowledgeChunk> chunks = knowledgeChunkRepository.searchByFullText(fullTextQuery, query.topK());
        for (KnowledgeChunk chunk : chunks) {
            RetrievalCandidate candidate = RetrievalCandidate.fromChunk(chunk, "bm25");
            candidate.setBm25Score(safeScore(chunk.getId(), fullTextQuery));
            candidate.addHitReason("bm25_fulltext");
            candidates.add(candidate);
        }
        return candidates;
    }

    private String buildFullTextQuery(String rawQuery) {
        List<String> tokens = textProcessingService.tokenize(rawQuery).stream()
                .filter(token -> token.length() >= 2)
                .limit(8)
                .toList();
        if (tokens.isEmpty()) {
            return rawQuery;
        }
        return String.join(" ", tokens);
    }

    private double safeScore(Long chunkId, String query) {
        try {
            return knowledgeChunkRepository.bm25Score(chunkId, query);
        } catch (RuntimeException exception) {
            return 0;
        }
    }
}
