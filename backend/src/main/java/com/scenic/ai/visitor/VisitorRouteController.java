package com.scenic.ai.visitor;

import com.scenic.ai.common.ApiResponse;
import com.scenic.ai.route.RouteRecommendationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visitor/routes")
public class VisitorRouteController {

    private final RouteRecommendationService routeRecommendationService;

    public VisitorRouteController(RouteRecommendationService routeRecommendationService) {
        this.routeRecommendationService = routeRecommendationService;
    }

    @PostMapping("/recommend")
    public ApiResponse<RouteRecommendationService.RouteRecommendation> recommend(@Valid @RequestBody RouteRecommendRequest request) {
        return ApiResponse.ok(routeRecommendationService.recommend(new RouteRecommendationService.RouteRecommendRequest(
                request.sessionToken(),
                request.interests(),
                request.durationMinutes()
        )));
    }

    public record RouteRecommendRequest(@NotBlank String sessionToken, List<String> interests, Integer durationMinutes) {
    }
}
