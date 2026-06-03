package com.scenic.ai.rag.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scenic.ai.rag.model.RetrievalCandidate;
import com.scenic.ai.rag.model.RetrievalQuery;
import com.scenic.ai.rag.model.RetrievalResult;
import com.scenic.ai.rag.model.RetrievalTrace;
import com.scenic.ai.rag.repository.RetrievalTraceRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HybridRetrievalService {

    private final Bm25RetrievalService bm25RetrievalService;
    private final KeywordRetrievalService keywordRetrievalService;
    private final VectorRetrievalService vectorRetrievalService;
    private final RetrievalFusionService retrievalFusionService;
    private final LowConfidenceGuard lowConfidenceGuard;
    private final RetrievalTraceRepository retrievalTraceRepository;
    private final ObjectMapper objectMapper;

    public HybridRetrievalService(
            Bm25RetrievalService bm25RetrievalService,
            KeywordRetrievalService keywordRetrievalService,
            VectorRetrievalService vectorRetrievalService,
            RetrievalFusionService retrievalFusionService,
            LowConfidenceGuard lowConfidenceGuard,
            RetrievalTraceRepository retrievalTraceRepository,
            ObjectMapper objectMapper
    ) {
        this.bm25RetrievalService = bm25RetrievalService;
        this.keywordRetrievalService = keywordRetrievalService;
        this.vectorRetrievalService = vectorRetrievalService;
        this.retrievalFusionService = retrievalFusionService;
        this.lowConfidenceGuard = lowConfidenceGuard;
        this.retrievalTraceRepository = retrievalTraceRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RetrievalResult search(RetrievalQuery query) {
        List<RetrievalCandidate> bm25 = bm25RetrievalService.retrieve(query);
        List<RetrievalCandidate> keyword = keywordRetrievalService.retrieve(query);
        List<RetrievalCandidate> vector = vectorRetrievalService.retrieve(query);
        List<RetrievalCandidate> finals = retrievalFusionService.fuse(query, bm25, keyword, vector);
        LowConfidenceGuard.Rejection rejection = lowConfidenceGuard.evaluate(query, finals);
        RetrievalTrace trace = retrievalTraceRepository.save(new RetrievalTrace(
                query.query(),
                toJson(bm25),
                toJson(keyword),
                toJson(vector),
                toJson(finals),
                rejection.rejected(),
                rejection.reason()
        ));
        return new RetrievalResult(
                trace.getId(),
                query.query(),
                bm25,
                keyword,
                vector,
                finals,
                rejection.rejected(),
                rejection.reason()
        );
    }

    private String toJson(List<RetrievalCandidate> candidates) {
        try {
            return objectMapper.writeValueAsString(candidates);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("检索 trace 序列化失败", exception);
        }
    }
}
