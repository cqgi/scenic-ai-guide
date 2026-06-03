package com.scenic.ai.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scenic.ai.interaction.Feedback;
import com.scenic.ai.interaction.FeedbackRepository;
import com.scenic.ai.interaction.InteractionLog;
import com.scenic.ai.interaction.InteractionLogRepository;
import com.scenic.ai.rag.model.KnowledgeChunk;
import com.scenic.ai.rag.model.RetrievalCandidate;
import com.scenic.ai.rag.model.RetrievalTrace;
import com.scenic.ai.rag.repository.KnowledgeChunkRepository;
import com.scenic.ai.rag.repository.RetrievalTraceRepository;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class InteractionAdminService {

    private final InteractionLogRepository interactionLogRepository;
    private final FeedbackRepository feedbackRepository;
    private final RetrievalTraceRepository retrievalTraceRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final ObjectMapper objectMapper;

    public InteractionAdminService(
            InteractionLogRepository interactionLogRepository,
            FeedbackRepository feedbackRepository,
            RetrievalTraceRepository retrievalTraceRepository,
            KnowledgeChunkRepository chunkRepository,
            ObjectMapper objectMapper
    ) {
        this.interactionLogRepository = interactionLogRepository;
        this.feedbackRepository = feedbackRepository;
        this.retrievalTraceRepository = retrievalTraceRepository;
        this.chunkRepository = chunkRepository;
        this.objectMapper = objectMapper;
    }

    public PageResponse<InteractionSummaryResponse> search(String keyword, String intent, String emotion,
                                                           Integer satisfaction, int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<InteractionLog> result = interactionLogRepository.searchAdmin(
                blankToNull(keyword),
                blankToNull(intent),
                blankToNull(emotion),
                satisfaction,
                pageRequest
        );
        return new PageResponse<>(
                result.getContent().stream().map(InteractionSummaryResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public InteractionDetailResponse detail(Long id) {
        InteractionLog log = interactionLogRepository.findWithSessionById(id)
                .orElseThrow(() -> new IllegalArgumentException("交互记录不存在"));
        List<Long> chunkIds = parseChunkIds(log.getSourceChunkIds());
        List<SourceChunkResponse> sources = chunkIds.isEmpty()
                ? List.of()
                : chunkRepository.findByIdIn(chunkIds).stream().map(SourceChunkResponse::from).toList();
        FeedbackResponse feedback = feedbackRepository.findTopByInteractionIdOrderByCreatedAtDesc(id)
                .map(FeedbackResponse::from)
                .orElse(null);
        RetrievalTraceResponse trace = retrievalTraceRepository.findByInteractionId(id)
                .map(this::traceResponse)
                .orElse(null);
        return new InteractionDetailResponse(InteractionSummaryResponse.from(log), sources, feedback, trace);
    }

    private RetrievalTraceResponse traceResponse(RetrievalTrace trace) {
        return new RetrievalTraceResponse(
                trace.getId(),
                trace.getInteractionId(),
                trace.getQuery(),
                readCandidates(trace.getBm25Candidates()),
                readCandidates(trace.getKeywordCandidates()),
                readCandidates(trace.getVectorCandidates()),
                readCandidates(trace.getFinalCandidates()),
                trace.isRejected(),
                trace.getRejectReason(),
                trace.getCreatedAt()
        );
    }

    private List<RetrievalCandidate> readCandidates(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("检索 trace 解析失败", exception);
        }
    }

    private List<Long> parseChunkIds(String sourceChunkIds) {
        if (sourceChunkIds == null || sourceChunkIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(sourceChunkIds.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> {
                    try {
                        return Long.parseLong(value);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record PageResponse<T>(List<T> items, int page, int size, long total, int totalPages) {
    }

    public record InteractionSummaryResponse(
            Long id,
            Long sessionId,
            String sessionToken,
            String scenicAreaName,
            String inputType,
            String userQuery,
            String asrText,
            String intent,
            String answer,
            String emotion,
            String sourceChunkIds,
            Integer latencyMs,
            Integer satisfaction,
            OffsetDateTime createdAt
    ) {
        static InteractionSummaryResponse from(InteractionLog log) {
            return new InteractionSummaryResponse(
                    log.getId(),
                    log.getSession().getId(),
                    log.getSession().getSessionToken(),
                    log.getSession().getScenicArea().getName(),
                    log.getInputType(),
                    log.getUserQuery(),
                    log.getAsrText(),
                    log.getIntent(),
                    log.getAnswer(),
                    log.getEmotion(),
                    log.getSourceChunkIds(),
                    log.getLatencyMs(),
                    log.getSatisfaction(),
                    log.getCreatedAt()
            );
        }
    }

    public record InteractionDetailResponse(
            InteractionSummaryResponse interaction,
            List<SourceChunkResponse> sourceChunks,
            FeedbackResponse feedback,
            RetrievalTraceResponse trace
    ) {
    }

    public record SourceChunkResponse(
            Long id,
            Long documentId,
            String documentTitle,
            String title,
            String content,
            String summary,
            String keywords,
            String spotNames,
            boolean enabled
    ) {
        static SourceChunkResponse from(KnowledgeChunk chunk) {
            return new SourceChunkResponse(
                    chunk.getId(),
                    chunk.getDocument().getId(),
                    chunk.getDocument().getTitle(),
                    chunk.getTitle(),
                    chunk.getContent(),
                    chunk.getSummary(),
                    chunk.getKeywords(),
                    chunk.getSpotNames(),
                    chunk.isEnabled()
            );
        }
    }

    public record FeedbackResponse(Long id, Integer score, String comment, String sentiment, OffsetDateTime createdAt) {
        static FeedbackResponse from(Feedback feedback) {
            return new FeedbackResponse(
                    feedback.getId(),
                    feedback.getScore(),
                    feedback.getComment(),
                    feedback.getSentiment(),
                    feedback.getCreatedAt()
            );
        }
    }

    public record RetrievalTraceResponse(
            Long id,
            Long interactionId,
            String query,
            List<RetrievalCandidate> bm25Candidates,
            List<RetrievalCandidate> keywordCandidates,
            List<RetrievalCandidate> vectorCandidates,
            List<RetrievalCandidate> finalCandidates,
            boolean rejected,
            String rejectReason,
            OffsetDateTime createdAt
    ) {
    }
}
