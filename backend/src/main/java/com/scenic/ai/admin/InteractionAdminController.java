package com.scenic.ai.admin;

import com.scenic.ai.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/interactions")
public class InteractionAdminController {

    private final InteractionAdminService interactionAdminService;

    public InteractionAdminController(InteractionAdminService interactionAdminService) {
        this.interactionAdminService = interactionAdminService;
    }

    @GetMapping
    public ApiResponse<InteractionAdminService.PageResponse<InteractionAdminService.InteractionSummaryResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String intent,
            @RequestParam(required = false) String emotion,
            @RequestParam(required = false) Integer satisfaction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(interactionAdminService.search(keyword, intent, emotion, satisfaction, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<InteractionAdminService.InteractionDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(interactionAdminService.detail(id));
    }
}
