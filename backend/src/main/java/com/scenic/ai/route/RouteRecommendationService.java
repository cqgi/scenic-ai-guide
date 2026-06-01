package com.scenic.ai.route;

import com.scenic.ai.ai.LlmClient;
import com.scenic.ai.ai.PromptService;
import com.scenic.ai.scenic.ScenicSpot;
import com.scenic.ai.scenic.ScenicSpotRepository;
import com.scenic.ai.visitor.VisitorSession;
import com.scenic.ai.visitor.VisitorSessionRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RouteRecommendationService {

    private final VisitorSessionRepository visitorSessionRepository;
    private final RoutePlanRepository routePlanRepository;
    private final ScenicSpotRepository scenicSpotRepository;
    private final PromptService promptService;
    private final LlmClient llmClient;

    public RouteRecommendationService(
            VisitorSessionRepository visitorSessionRepository,
            RoutePlanRepository routePlanRepository,
            ScenicSpotRepository scenicSpotRepository,
            PromptService promptService,
            LlmClient llmClient
    ) {
        this.visitorSessionRepository = visitorSessionRepository;
        this.routePlanRepository = routePlanRepository;
        this.scenicSpotRepository = scenicSpotRepository;
        this.promptService = promptService;
        this.llmClient = llmClient;
    }

    public RouteRecommendation recommend(RouteRecommendRequest request) {
        VisitorSession session = visitorSessionRepository.findWithScenicAreaBySessionToken(request.sessionToken())
                .orElseThrow(() -> new IllegalArgumentException("游客 session 不存在或已过期"));
        session.touch();
        visitorSessionRepository.save(session);
        List<RoutePlan> plans = routePlanRepository.findByScenicAreaIdAndEnabledTrue(session.getScenicArea().getId());
        if (plans.isEmpty()) {
            throw new IllegalArgumentException("当前景区暂无可推荐路线");
        }
        RoutePlan best = plans.stream()
                .max(Comparator.comparingDouble(plan -> score(plan, request)))
                .orElse(plans.get(0));
        Map<Long, ScenicSpot> spots = new LinkedHashMap<>();
        scenicSpotRepository.findByScenicAreaIdAndEnabledTrueOrderBySortOrderAsc(session.getScenicArea().getId())
                .forEach(spot -> spots.put(spot.getId(), spot));
        List<RouteSpot> routeSpots = parseSpotIds(best.getSpotIds()).stream()
                .map(spots::get)
                .filter(spot -> spot != null)
                .map(RouteSpot::from)
                .toList();
        String routeSummary = "%s，预计 %d 分钟，景点：%s。讲解：%s".formatted(
                best.getName(),
                best.getDurationMinutes(),
                String.join("、", routeSpots.stream().map(RouteSpot::name).toList()),
                best.getGuideText()
        );
        LlmClient.LlmResponse reason = llmClient.chat(
                promptService.routeSystemPrompt(),
                promptService.routeUserPrompt(String.join(",", request.interests()), request.durationMinutes(), routeSummary)
        );
        String recommendationReason = reason.fallback() ? promptService.routeFallbackReason(request.interests()) : reason.content();
        return new RouteRecommendation(best.getId(), best.getName(), best.getDurationMinutes(), routeSpots, recommendationReason, best.getGuideText());
    }

    private double score(RoutePlan plan, RouteRecommendRequest request) {
        double score = 0;
        String tags = plan.getInterestTags() == null ? "" : plan.getInterestTags();
        for (String interest : request.interests()) {
            if (tags.contains(interest)) {
                score += 3;
            }
        }
        if (request.durationMinutes() != null) {
            score -= Math.abs(plan.getDurationMinutes() - request.durationMinutes()) / 30.0;
        }
        return score;
    }

    private List<Long> parseSpotIds(String spotIds) {
        if (spotIds == null || spotIds.isBlank()) {
            return List.of();
        }
        return List.of(spotIds.split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Long::parseLong)
                .toList();
    }

    public record RouteRecommendRequest(String sessionToken, List<String> interests, Integer durationMinutes) {
        public RouteRecommendRequest {
            interests = interests == null ? List.of() : List.copyOf(interests);
        }
    }

    public record RouteRecommendation(Long routeId, String name, Integer durationMinutes, List<RouteSpot> spots,
                                      String reason, String guideText) {
    }

    public record RouteSpot(Long id, String name, String summary, Integer recommendedMinutes) {
        static RouteSpot from(ScenicSpot spot) {
            return new RouteSpot(spot.getId(), spot.getName(), spot.getSummary(), spot.getRecommendedMinutes());
        }
    }
}
