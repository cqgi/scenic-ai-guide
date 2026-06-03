package com.scenic.ai.rag.controller;

import com.scenic.ai.auth.AdminUser;
import com.scenic.ai.admin.KnowledgeAdminService;
import com.scenic.ai.common.ApiResponse;
import com.scenic.ai.rag.service.KnowledgeImportService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/admin/knowledge/documents")
public class KnowledgeDocumentController {

    private final KnowledgeImportService knowledgeImportService;
    private final KnowledgeAdminService knowledgeAdminService;

    public KnowledgeDocumentController(KnowledgeImportService knowledgeImportService, KnowledgeAdminService knowledgeAdminService) {
        this.knowledgeImportService = knowledgeImportService;
        this.knowledgeAdminService = knowledgeAdminService;
    }

    @GetMapping
    public ApiResponse<List<KnowledgeAdminService.KnowledgeDocumentResponse>> documents(
            @RequestParam(defaultValue = "1") Long scenicAreaId
    ) {
        return ApiResponse.ok(knowledgeAdminService.documents(scenicAreaId));
    }

    @PostMapping
    public ApiResponse<UploadKnowledgeDocumentResponse> upload(
            @RequestParam @NotNull Long scenicAreaId,
            @RequestParam @NotBlank String title,
            @RequestParam MultipartFile file,
            @AuthenticationPrincipal AdminUser adminUser
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        KnowledgeImportService.ImportResult result = knowledgeImportService.importFile(
                scenicAreaId,
                title,
                file,
                adminUser == null ? null : adminUser.getId()
        );
        return ApiResponse.ok(new UploadKnowledgeDocumentResponse(result.documentId(), result.chunkCount(), result.chunkIds()));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<KnowledgeAdminService.KnowledgeDocumentResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateDocumentStatusRequest request
    ) {
        return ApiResponse.ok(knowledgeAdminService.updateStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        knowledgeAdminService.deleteDocument(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/chunks")
    public ApiResponse<List<KnowledgeAdminService.KnowledgeChunkResponse>> chunks(@PathVariable Long id) {
        return ApiResponse.ok(knowledgeAdminService.chunks(id));
    }

    public record UploadKnowledgeDocumentResponse(Long documentId, int chunkCount, List<Long> chunkIds) {
    }

    public record UpdateDocumentStatusRequest(@NotBlank String status) {
    }

    public record UpdateChunkEnabledRequest(boolean enabled) {
    }
}
