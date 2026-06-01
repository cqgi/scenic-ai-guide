package com.scenic.ai.rag.controller;

import com.scenic.ai.auth.AdminUser;
import com.scenic.ai.common.ApiResponse;
import com.scenic.ai.rag.service.KnowledgeImportService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/admin/knowledge/documents")
public class KnowledgeDocumentController {

    private final KnowledgeImportService knowledgeImportService;

    public KnowledgeDocumentController(KnowledgeImportService knowledgeImportService) {
        this.knowledgeImportService = knowledgeImportService;
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

    public record UploadKnowledgeDocumentResponse(Long documentId, int chunkCount, List<Long> chunkIds) {
    }
}
