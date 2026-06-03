package com.scenic.ai.rag.model;

import java.util.List;

public record RetrievalResult(
        Long traceId,
        String query,
        List<RetrievalCandidate> bm25Candidates,
        List<RetrievalCandidate> keywordCandidates,
        List<RetrievalCandidate> vectorCandidates,
        List<RetrievalCandidate> finalCandidates,
        boolean rejected,
        String rejectReason
) {
    public RetrievalResult(
            String query,
            List<RetrievalCandidate> bm25Candidates,
            List<RetrievalCandidate> keywordCandidates,
            List<RetrievalCandidate> vectorCandidates,
            List<RetrievalCandidate> finalCandidates,
            boolean rejected,
            String rejectReason
    ) {
        this(null, query, bm25Candidates, keywordCandidates, vectorCandidates, finalCandidates, rejected, rejectReason);
    }
}
