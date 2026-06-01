package com.scenic.ai.rag.repository;

import com.scenic.ai.rag.model.KnowledgeDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {
    List<KnowledgeDocument> findByScenicAreaIdOrderByCreatedAtDesc(Long scenicAreaId);
}
