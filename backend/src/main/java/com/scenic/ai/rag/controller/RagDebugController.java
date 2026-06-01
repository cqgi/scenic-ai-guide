package com.scenic.ai.rag.controller;

import com.scenic.ai.common.ApiResponse;
import com.scenic.ai.rag.config.RagProperties;
import com.scenic.ai.rag.model.RetrievalQuery;
import com.scenic.ai.rag.model.RetrievalResult;
import com.scenic.ai.rag.service.HybridRetrievalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/debug/rag")
public class RagDebugController {

    private final HybridRetrievalService hybridRetrievalService;
    private final RagProperties ragProperties;

    public RagDebugController(HybridRetrievalService hybridRetrievalService, RagProperties ragProperties) {
        this.hybridRetrievalService = hybridRetrievalService;
        this.ragProperties = ragProperties;
    }

    @PostMapping("/search")
    public ApiResponse<RetrievalResult> search(@Valid @RequestBody RagSearchRequest request) {
        RetrievalQuery query = new RetrievalQuery(
                request.query(),
                request.interests(),
                clamp(request.effectiveTopK(), ragProperties.maxTopK()),
                clamp(request.effectiveTopN(), ragProperties.maxTopN())
        );
        return ApiResponse.ok(hybridRetrievalService.search(query));
    }

    public record RagSearchRequest(@NotBlank String query, List<String> interests, Integer topK, Integer topN) {
        int effectiveTopK() {
            return topK == null ? 10 : topK;
        }

        int effectiveTopN() {
            return topN == null ? 6 : topN;
        }
    }

    private int clamp(int value, int max) {
        return Math.max(1, Math.min(value, max));
    }
}
