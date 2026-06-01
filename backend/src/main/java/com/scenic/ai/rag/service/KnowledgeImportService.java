package com.scenic.ai.rag.service;

import com.scenic.ai.auth.AdminUser;
import com.scenic.ai.auth.AdminUserRepository;
import com.scenic.ai.rag.model.KnowledgeChunk;
import com.scenic.ai.rag.model.KnowledgeDocument;
import com.scenic.ai.rag.repository.KnowledgeChunkRepository;
import com.scenic.ai.rag.repository.KnowledgeDocumentRepository;
import com.scenic.ai.scenic.ScenicArea;
import com.scenic.ai.scenic.ScenicAreaRepository;
import com.scenic.ai.scenic.ScenicSpot;
import com.scenic.ai.scenic.ScenicSpotRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class KnowledgeImportService {

    private final DocumentParserService documentParserService;
    private final TextProcessingService textProcessingService;
    private final EmbeddingService embeddingService;
    private final PgVectorFormatter pgVectorFormatter;
    private final ScenicAreaRepository scenicAreaRepository;
    private final ScenicSpotRepository scenicSpotRepository;
    private final AdminUserRepository adminUserRepository;
    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final KnowledgeChunkRepository knowledgeChunkRepository;
    private final EntityManager entityManager;

    public KnowledgeImportService(
            DocumentParserService documentParserService,
            TextProcessingService textProcessingService,
            EmbeddingService embeddingService,
            PgVectorFormatter pgVectorFormatter,
            ScenicAreaRepository scenicAreaRepository,
            ScenicSpotRepository scenicSpotRepository,
            AdminUserRepository adminUserRepository,
            KnowledgeDocumentRepository knowledgeDocumentRepository,
            KnowledgeChunkRepository knowledgeChunkRepository,
            EntityManager entityManager
    ) {
        this.documentParserService = documentParserService;
        this.textProcessingService = textProcessingService;
        this.embeddingService = embeddingService;
        this.pgVectorFormatter = pgVectorFormatter;
        this.scenicAreaRepository = scenicAreaRepository;
        this.scenicSpotRepository = scenicSpotRepository;
        this.adminUserRepository = adminUserRepository;
        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
        this.knowledgeChunkRepository = knowledgeChunkRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public ImportResult importFile(Long scenicAreaId, String title, MultipartFile file, Long adminUserId) {
        DocumentParserService.ParsedDocument parsed = documentParserService.parse(file);
        return importText(scenicAreaId, title, parsed.fileName(), parsed.docType(), parsed.text(), adminUserId);
    }

    @Transactional
    public ImportResult importText(Long scenicAreaId, String title, String fileName, String docType, String text, Long adminUserId) {
        ScenicArea area = scenicAreaRepository.findById(scenicAreaId)
                .filter(ScenicArea::isEnabled)
                .orElseThrow(() -> new IllegalArgumentException("景区不存在或已停用"));
        AdminUser adminUser = adminUserId == null ? null : adminUserRepository.findById(adminUserId).orElse(null);
        KnowledgeDocument document = knowledgeDocumentRepository.save(new KnowledgeDocument(area, title, fileName, docType, adminUser));

        List<ScenicSpot> spots = scenicSpotRepository.findByScenicAreaIdAndEnabledTrueOrderBySortOrderAsc(scenicAreaId);
        List<String> chunks = textProcessingService.splitChunks(text);
        List<Long> chunkIds = new ArrayList<>();
        int index = 1;
        for (String chunkText : chunks) {
            TextProcessingService.ExtractedTerms terms = textProcessingService.extractTerms(chunkText, spots);
            ScenicSpot matchedSpot = matchSpot(terms.spotNames(), spots);
            String chunkTitle = buildChunkTitle(title, chunkText, index++);
            String tags = matchedSpot == null ? "" : matchedSpot.getTags();
            String searchText = textProcessingService.buildSearchText(chunkTitle, chunkText, terms, tags);
            KnowledgeChunk chunk = knowledgeChunkRepository.save(new KnowledgeChunk(
                    document,
                    matchedSpot,
                    chunkTitle,
                    chunkText,
                    summarize(chunkText),
                    terms.keywords(),
                    terms.aliases(),
                    terms.spotNames(),
                    tags,
                    searchText,
                    "{\"source\":\"upload\"}"
            ));
            writeEmbedding(chunk.getId(), embeddingService.embed(searchText));
            chunkIds.add(chunk.getId());
        }
        return new ImportResult(document.getId(), chunkIds.size(), chunkIds);
    }

    private ScenicSpot matchSpot(String spotNames, List<ScenicSpot> spots) {
        if (spotNames == null || spotNames.isBlank()) {
            return null;
        }
        for (ScenicSpot spot : spots) {
            if (spotNames.contains(spot.getName())) {
                return spot;
            }
        }
        return null;
    }

    private String buildChunkTitle(String title, String chunkText, int index) {
        String firstLine = chunkText.lines().findFirst().orElse("").replaceAll("^#+\\s*", "").trim();
        if (!firstLine.isBlank() && firstLine.length() <= 40) {
            return firstLine;
        }
        return title + " #" + index;
    }

    private String summarize(String chunkText) {
        String normalized = chunkText.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 120 ? normalized : normalized.substring(0, 120);
    }

    private void writeEmbedding(Long chunkId, double[] embedding) {
        entityManager.createNativeQuery("UPDATE knowledge_chunk SET embedding = CAST(:embedding AS vector) WHERE id = :id")
                .setParameter("embedding", pgVectorFormatter.format(embedding))
                .setParameter("id", chunkId)
                .executeUpdate();
    }

    public record ImportResult(Long documentId, int chunkCount, List<Long> chunkIds) {
    }
}
