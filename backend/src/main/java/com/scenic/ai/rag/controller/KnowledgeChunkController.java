package com.scenic.ai.rag.controller;

import com.scenic.ai.admin.KnowledgeAdminService;
import com.scenic.ai.common.ApiResponse;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/knowledge/chunks")
public class KnowledgeChunkController {

    private final KnowledgeAdminService knowledgeAdminService;

    public KnowledgeChunkController(KnowledgeAdminService knowledgeAdminService) {
        this.knowledgeAdminService = knowledgeAdminService;
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<KnowledgeAdminService.KnowledgeChunkResponse> updateChunkEnabled(
            @PathVariable Long id,
            @RequestBody UpdateChunkEnabledRequest request
    ) {
        return ApiResponse.ok(knowledgeAdminService.updateChunkEnabled(id, request.enabled()));
    }

    public record UpdateChunkEnabledRequest(boolean enabled) {
    }
}
