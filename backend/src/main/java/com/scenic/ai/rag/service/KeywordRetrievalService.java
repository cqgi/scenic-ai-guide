package com.scenic.ai.rag.service;

import com.scenic.ai.rag.model.KnowledgeChunk;
import com.scenic.ai.rag.model.RetrievalCandidate;
import com.scenic.ai.rag.model.RetrievalQuery;
import com.scenic.ai.rag.repository.KnowledgeChunkRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class KeywordRetrievalService {

    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final TextProcessingService textProcessingService;

    public KeywordRetrievalService(KnowledgeChunkRepository knowledgeChunkRepository, TextProcessingService textProcessingService) {
        this.knowledgeChunkRepository = knowledgeChunkRepository;
        this.textProcessingService = textProcessingService;
    }

    public List<RetrievalCandidate> retrieve(RetrievalQuery query) {
        Set<String> terms = new LinkedHashSet<>(textProcessingService.tokenize(query.query()));
        terms.add(query.query().trim());
        Map<Long, KnowledgeChunk> preselected = new LinkedHashMap<>();
        for (String term : terms) {
            if (term.isBlank()) {
                continue;
            }
            for (KnowledgeChunk chunk : knowledgeChunkRepository.findKeywordCandidates(term, query.topK() * 5)) {
                preselected.putIfAbsent(chunk.getId(), chunk);
            }
            if (preselected.size() >= query.topK() * 10) {
                break;
            }
        }
        return preselected.values().stream()
                .map(chunk -> score(chunk, terms, query))
                .filter(candidate -> candidate.getKeywordScore() > 0)
                .sorted(Comparator.comparingDouble(RetrievalCandidate::getKeywordScore).reversed())
                .limit(query.topK())
                .toList();
    }

    private RetrievalCandidate score(KnowledgeChunk chunk, Set<String> terms, RetrievalQuery query) {
        RetrievalCandidate candidate = RetrievalCandidate.fromChunk(chunk, "keyword");
        String title = lower(chunk.getTitle());
        String content = lower(chunk.getContent());
        String searchText = lower(chunk.getSearchText());
        String spotNames = lower(chunk.getSpotNames());
        String aliases = lower(chunk.getAliases());
        String keywords = lower(chunk.getKeywords());

        double score = 0;
        boolean lexicalHit = false;
        for (String rawTerm : terms) {
            String term = lower(rawTerm);
            if (term.isBlank()) {
                continue;
            }
            if (title.contains(term)) {
                score += 2.2;
                lexicalHit = true;
                candidate.addHitReason("title:" + rawTerm);
            }
            if (spotNames.contains(term)) {
                score += 2.0;
                lexicalHit = true;
                candidate.addHitReason("spot:" + rawTerm);
            }
            if (aliases.contains(term)) {
                score += 1.8;
                lexicalHit = true;
                candidate.addHitReason("alias:" + rawTerm);
            }
            if (keywords.contains(term)) {
                score += 1.2;
                lexicalHit = true;
                candidate.addHitReason("keyword:" + rawTerm);
            }
            if (content.contains(term)) {
                score += 0.7;
                lexicalHit = true;
            } else if (searchText.contains(term)) {
                score += 0.5;
                lexicalHit = true;
            }
        }

        if (lexicalHit) {
            for (String interest : query.interests()) {
                String normalized = lower(interest);
                if (!normalized.isBlank() && lower(chunk.getTags()).contains(normalized)) {
                    score += 0.8;
                    candidate.addHitReason("interest:" + interest);
                }
            }
        }
        candidate.setKeywordScore(score);
        return candidate;
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
