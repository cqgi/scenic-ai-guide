package com.scenic.ai.rag.service;

import com.scenic.ai.rag.model.RetrievalCandidate;
import com.scenic.ai.rag.model.RetrievalQuery;
import com.scenic.ai.scenic.ScenicSpot;
import com.scenic.ai.scenic.ScenicSpotRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LowConfidenceGuard {

    private static final double MIN_TOP_SCORE = 0.35;
    private static final List<String> REAL_TIME_TERMS = List.of("今天", "现在", "实时", "天气", "排队", "拥堵", "开了吗", "开放吗", "免票", "票价", "几点关门");

    private final ScenicSpotRepository scenicSpotRepository;

    public LowConfidenceGuard(ScenicSpotRepository scenicSpotRepository) {
        this.scenicSpotRepository = scenicSpotRepository;
    }

    public Rejection evaluate(RetrievalQuery query, List<RetrievalCandidate> finalCandidates) {
        if (finalCandidates.isEmpty()) {
            return new Rejection(true, "没有检索到可引用的知识片段");
        }
        RetrievalCandidate top = finalCandidates.get(0);
        if (top.getFinalScore() < MIN_TOP_SCORE) {
            return new Rejection(true, "最高置信度低于阈值");
        }
        if (containsRealtimeTerm(query.query())) {
            return new Rejection(true, "问题涉及实时信息，当前知识库证据不足");
        }
        String explicitSpot = explicitSpot(query.query());
        if (explicitSpot != null && !contains(top, explicitSpot) && top.getFinalScore() < 0.75) {
            return new Rejection(true, "问题指定景点与检索结果不匹配");
        }
        return new Rejection(false, null);
    }

    private boolean containsRealtimeTerm(String query) {
        return REAL_TIME_TERMS.stream().anyMatch(query::contains);
    }

    private String explicitSpot(String query) {
        for (ScenicSpot spot : scenicSpotRepository.findAll()) {
            if (query.contains(spot.getName())) {
                return spot.getName();
            }
            if (spot.getAlias() == null) {
                continue;
            }
            for (String alias : spot.getAlias().split(",")) {
                String value = alias.trim();
                if (!value.isEmpty() && query.contains(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    private boolean contains(RetrievalCandidate candidate, String value) {
        return candidate.getTitle().contains(value) || candidate.getContent().contains(value);
    }

    public record Rejection(boolean rejected, String reason) {
    }
}
