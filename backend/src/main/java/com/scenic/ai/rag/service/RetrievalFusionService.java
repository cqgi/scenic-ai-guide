package com.scenic.ai.rag.service;

import com.scenic.ai.rag.model.RetrievalCandidate;
import com.scenic.ai.rag.model.RetrievalQuery;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RetrievalFusionService {

    private static final double BM25_WEIGHT = 0.40;
    private static final double KEYWORD_WEIGHT = 0.25;
    private static final double VECTOR_WEIGHT = 0.30;
    private static final double METADATA_WEIGHT = 0.05;

    public List<RetrievalCandidate> fuse(
            RetrievalQuery query,
            List<RetrievalCandidate> bm25Candidates,
            List<RetrievalCandidate> keywordCandidates,
            List<RetrievalCandidate> vectorCandidates
    ) {
        Map<Long, RetrievalCandidate> merged = new LinkedHashMap<>();
        merge(merged, bm25Candidates);
        merge(merged, keywordCandidates);
        merge(merged, vectorCandidates);

        double maxBm25 = max(merged.values().stream().mapToDouble(RetrievalCandidate::getBm25Score).toArray());
        double maxKeyword = max(merged.values().stream().mapToDouble(RetrievalCandidate::getKeywordScore).toArray());
        double maxVector = max(merged.values().stream().mapToDouble(RetrievalCandidate::getVectorScore).toArray());

        for (RetrievalCandidate candidate : merged.values()) {
            double metadataBoost = metadataBoost(query, candidate);
            candidate.setMetadataBoost(metadataBoost);
            candidate.setFinalScore(
                    BM25_WEIGHT * normalize(candidate.getBm25Score(), maxBm25)
                            + KEYWORD_WEIGHT * normalize(candidate.getKeywordScore(), maxKeyword)
                            + VECTOR_WEIGHT * normalize(candidate.getVectorScore(), maxVector)
                            + METADATA_WEIGHT * metadataBoost
            );
        }

        return merged.values().stream()
                .sorted(Comparator.comparingDouble(RetrievalCandidate::getFinalScore).reversed())
                .limit(query.topN())
                .toList();
    }

    private void merge(Map<Long, RetrievalCandidate> target, List<RetrievalCandidate> source) {
        for (RetrievalCandidate incoming : source) {
            RetrievalCandidate existing = target.get(incoming.getChunkId());
            if (existing == null) {
                target.put(incoming.getChunkId(), incoming.copy());
                continue;
            }
            existing.setSourceType(existing.getSourceType() + "+" + incoming.getSourceType());
            existing.setBm25Score(Math.max(existing.getBm25Score(), incoming.getBm25Score()));
            existing.setKeywordScore(Math.max(existing.getKeywordScore(), incoming.getKeywordScore()));
            existing.setVectorScore(Math.max(existing.getVectorScore(), incoming.getVectorScore()));
            for (String reason : incoming.getHitReasons()) {
                existing.addHitReason(reason);
            }
        }
    }

    private double metadataBoost(RetrievalQuery query, RetrievalCandidate candidate) {
        String queryText = lower(query.query());
        String title = lower(candidate.getTitle());
        String content = lower(candidate.getContent());
        double boost = 0;
        for (String token : new ArrayList<>(List.of(queryText.split("\\s+")))) {
            if (token.isBlank()) {
                continue;
            }
            if (title.contains(token)) {
                boost += 0.25;
            }
            if (content.contains(token)) {
                boost += 0.1;
            }
        }
        if (candidate.getSourceType().contains("bm25") && candidate.getSourceType().contains("keyword")) {
            boost += 0.25;
        }
        if (candidate.getSourceType().contains("vector") && candidate.getSourceType().contains("keyword")) {
            boost += 0.2;
        }
        return Math.min(boost, 1.0);
    }

    private double normalize(double score, double max) {
        if (max <= 0) {
            return 0;
        }
        return Math.min(score / max, 1.0);
    }

    private double max(double[] values) {
        double max = 0;
        for (double value : values) {
            max = Math.max(max, value);
        }
        return max;
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
