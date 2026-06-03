package com.scenic.ai.admin;

import com.scenic.ai.interaction.Feedback;
import com.scenic.ai.interaction.FeedbackRepository;
import com.scenic.ai.interaction.InteractionLog;
import com.scenic.ai.interaction.InteractionLogRepository;
import com.scenic.ai.rag.repository.KnowledgeDocumentRepository;
import com.scenic.ai.rag.repository.RetrievalTraceRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class DashboardAdminService {

    private final InteractionLogRepository interactionLogRepository;
    private final FeedbackRepository feedbackRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final RetrievalTraceRepository retrievalTraceRepository;
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");

    public DashboardAdminService(
            InteractionLogRepository interactionLogRepository,
            FeedbackRepository feedbackRepository,
            KnowledgeDocumentRepository documentRepository,
            RetrievalTraceRepository retrievalTraceRepository
    ) {
        this.interactionLogRepository = interactionLogRepository;
        this.feedbackRepository = feedbackRepository;
        this.documentRepository = documentRepository;
        this.retrievalTraceRepository = retrievalTraceRepository;
    }

    public OverviewResponse overview() {
        long totalInteractions = interactionLogRepository.count();
        OffsetDateTime todayStart = LocalDate.now(BUSINESS_ZONE).atStartOfDay(BUSINESS_ZONE).toOffsetDateTime();
        long todayInteractions = interactionLogRepository.countByCreatedAtAfter(todayStart);
        long routeRequests = interactionLogRepository.countByIntent("route_recommendation");
        long activeDocuments = documentRepository.countByStatus("active");
        double averageSatisfaction = round(interactionLogRepository.averageSatisfaction());
        double averageLatencyMs = round(interactionLogRepository.averageLatencyMs());
        long rejectedCount = retrievalTraceRepository.countByRejectedTrue();
        double rejectRate = totalInteractions == 0 ? 0 : round((double) rejectedCount * 100 / totalInteractions);
        return new OverviewResponse(
                totalInteractions,
                todayInteractions,
                routeRequests,
                activeDocuments,
                averageSatisfaction,
                rejectRate,
                averageLatencyMs,
                serviceAdvice(rejectedCount)
        );
    }

    public TrendsResponse trends(int days) {
        int safeDays = Math.min(Math.max(days, 1), 30);
        LocalDate startDate = LocalDate.now(BUSINESS_ZONE).minusDays(safeDays - 1L);
        OffsetDateTime start = startDate.atStartOfDay(BUSINESS_ZONE).toOffsetDateTime();
        List<InteractionLog> logs = interactionLogRepository.findSince(start);
        Map<LocalDate, TrendPoint> trendMap = new LinkedHashMap<>();
        for (int i = 0; i < safeDays; i++) {
            LocalDate day = startDate.plusDays(i);
            trendMap.put(day, new TrendPoint(day.toString(), 0, null));
        }
        Map<String, Long> emotions = new LinkedHashMap<>();
        for (InteractionLog log : logs) {
            LocalDate day = log.getCreatedAt().atZoneSameInstant(BUSINESS_ZONE).toLocalDate();
            TrendPoint current = trendMap.get(day);
            if (current != null) {
                trendMap.put(day, current.add(log.getSatisfaction()));
            }
            String emotion = log.getEmotion() == null ? "unknown" : log.getEmotion();
            emotions.put(emotion, emotions.getOrDefault(emotion, 0L) + 1);
        }
        return new TrendsResponse(new ArrayList<>(trendMap.values()), emotions);
    }

    public List<HotQuestionResponse> hotQuestions(int limit) {
        return interactionLogRepository.hotQuestions(PageRequest.of(0, Math.min(Math.max(limit, 1), 50)))
                .stream()
                .map(row -> new HotQuestionResponse((String) row[0], (Long) row[1]))
                .toList();
    }

    private List<String> serviceAdvice(long rejectedCount) {
        List<String> advice = new ArrayList<>();
        if (rejectedCount > 0) {
            advice.add("存在拒答问题，优先查看交互详情中的 trace，把高频拒答补充到知识库。");
        }
        List<Feedback> lowFeedback = feedbackRepository.findTop20ByScoreLessThanEqualOrderByCreatedAtDesc(3);
        if (!lowFeedback.isEmpty()) {
            advice.add("近期出现低分反馈，建议复核对应回答的来源 chunk 与讲解口径。");
        }
        if (documentRepository.countByStatus("active") < 2) {
            advice.add("当前启用知识文档较少，建议补充票务、交通、路线、景点故事类资料。");
        }
        if (advice.isEmpty()) {
            advice.add("当前服务状态稳定，可继续补充季节活动、拍照点和路线讲解资料。");
        }
        advice.add("每次更新知识库后，建议用游客端验证 3 个热门问题和 1 条路线推荐。");
        return advice.stream().limit(5).toList();
    }

    private double round(Double value) {
        if (value == null) {
            return 0;
        }
        return Math.round(value * 10.0) / 10.0;
    }

    public record OverviewResponse(
            long totalInteractions,
            long todayInteractions,
            long routeRequests,
            long activeDocuments,
            double averageSatisfaction,
            double rejectRate,
            double averageLatencyMs,
            List<String> serviceAdvice
    ) {
    }

    public record TrendsResponse(List<TrendPoint> daily, Map<String, Long> emotions) {
    }

    public record TrendPoint(String date, long interactions, Double averageSatisfaction, long satisfactionCount) {
        public TrendPoint(String date, long interactions, Double averageSatisfaction) {
            this(date, interactions, averageSatisfaction, 0);
        }

        TrendPoint add(Integer satisfaction) {
            if (satisfaction == null) {
                return new TrendPoint(date, interactions + 1, averageSatisfaction, satisfactionCount);
            }
            double total = averageSatisfaction == null ? 0 : averageSatisfaction * satisfactionCount;
            long nextCount = satisfactionCount + 1;
            double next = Math.round((total + satisfaction) / nextCount * 10.0) / 10.0;
            return new TrendPoint(date, interactions + 1, next, nextCount);
        }
    }

    public record HotQuestionResponse(String query, long count) {
    }
}
