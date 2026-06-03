package com.scenic.ai.admin;

import com.scenic.ai.rag.model.KnowledgeChunk;
import com.scenic.ai.rag.model.KnowledgeDocument;
import com.scenic.ai.rag.repository.KnowledgeChunkRepository;
import com.scenic.ai.rag.repository.KnowledgeDocumentRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeAdminService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;

    public KnowledgeAdminService(KnowledgeDocumentRepository documentRepository, KnowledgeChunkRepository chunkRepository) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
    }

    public List<KnowledgeDocumentResponse> documents(Long scenicAreaId) {
        return documentRepository.findByScenicAreaIdOrderByCreatedAtDesc(scenicAreaId)
                .stream()
                .map(document -> KnowledgeDocumentResponse.from(
                        document,
                        chunkRepository.countByDocumentId(document.getId()),
                        chunkRepository.countByDocumentIdAndEnabledTrue(document.getId())
                ))
                .toList();
    }

    @Transactional
    public KnowledgeDocumentResponse updateStatus(Long documentId, String status) {
        if (!List.of("active", "disabled").contains(status)) {
            throw new IllegalArgumentException("文档状态只能是 active 或 disabled");
        }
        KnowledgeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("知识库文档不存在"));
        document.setStatus(status);
        boolean enabled = "active".equals(status);
        chunkRepository.findByDocumentIdOrderByIdAsc(documentId).forEach(chunk -> chunk.setEnabled(enabled));
        return KnowledgeDocumentResponse.from(
                document,
                chunkRepository.countByDocumentId(document.getId()),
                chunkRepository.countByDocumentIdAndEnabledTrue(document.getId())
        );
    }

    @Transactional
    public void deleteDocument(Long documentId) {
        KnowledgeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("知识库文档不存在"));
        chunkRepository.deleteAll(chunkRepository.findByDocumentIdOrderByIdAsc(documentId));
        documentRepository.delete(document);
    }

    public List<KnowledgeChunkResponse> chunks(Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new IllegalArgumentException("知识库文档不存在");
        }
        return chunkRepository.findByDocumentIdOrderByIdAsc(documentId)
                .stream()
                .map(KnowledgeChunkResponse::from)
                .toList();
    }

    @Transactional
    public KnowledgeChunkResponse updateChunkEnabled(Long chunkId, boolean enabled) {
        KnowledgeChunk chunk = chunkRepository.findByIdWithDocumentAndScenicSpot(chunkId)
                .orElseThrow(() -> new IllegalArgumentException("知识片段不存在"));
        chunk.setEnabled(enabled);
        return KnowledgeChunkResponse.from(chunk);
    }

    public record KnowledgeDocumentResponse(
            Long id,
            Long scenicAreaId,
            String title,
            String fileName,
            String fileUrl,
            String docType,
            String status,
            long chunkCount,
            long enabledChunkCount,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        static KnowledgeDocumentResponse from(KnowledgeDocument document, long chunkCount, long enabledChunkCount) {
            return new KnowledgeDocumentResponse(
                    document.getId(),
                    document.getScenicArea().getId(),
                    document.getTitle(),
                    document.getFileName(),
                    document.getFileUrl(),
                    document.getDocType(),
                    document.getStatus(),
                    chunkCount,
                    enabledChunkCount,
                    document.getCreatedAt(),
                    document.getUpdatedAt()
            );
        }
    }

    public record KnowledgeChunkResponse(
            Long id,
            Long documentId,
            Long scenicSpotId,
            String scenicSpotName,
            String title,
            String content,
            String summary,
            String keywords,
            String aliases,
            String spotNames,
            String tags,
            String metadataJson,
            boolean enabled,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        static KnowledgeChunkResponse from(KnowledgeChunk chunk) {
            return new KnowledgeChunkResponse(
                    chunk.getId(),
                    chunk.getDocument().getId(),
                    chunk.getScenicSpot() == null ? null : chunk.getScenicSpot().getId(),
                    chunk.getScenicSpot() == null ? null : chunk.getScenicSpot().getName(),
                    chunk.getTitle(),
                    chunk.getContent(),
                    chunk.getSummary(),
                    chunk.getKeywords(),
                    chunk.getAliases(),
                    chunk.getSpotNames(),
                    chunk.getTags(),
                    chunk.getMetadataJson(),
                    chunk.isEnabled(),
                    chunk.getCreatedAt(),
                    chunk.getUpdatedAt()
            );
        }
    }
}
