package com.scenic.ai.admin;

import com.scenic.ai.common.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
public class DashboardAdminController {

    private final DashboardAdminService dashboardAdminService;

    public DashboardAdminController(DashboardAdminService dashboardAdminService) {
        this.dashboardAdminService = dashboardAdminService;
    }

    @GetMapping("/overview")
    public ApiResponse<DashboardAdminService.OverviewResponse> overview() {
        return ApiResponse.ok(dashboardAdminService.overview());
    }

    @GetMapping("/trends")
    public ApiResponse<DashboardAdminService.TrendsResponse> trends(@RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(dashboardAdminService.trends(days));
    }

    @GetMapping("/hot-questions")
    public ApiResponse<List<DashboardAdminService.HotQuestionResponse>> hotQuestions(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.ok(dashboardAdminService.hotQuestions(limit));
    }
}
