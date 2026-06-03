package com.scenic.ai.rag.repository;

import com.scenic.ai.rag.model.KnowledgeDocument;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {
    @EntityGraph(attributePaths = "scenicArea")
    List<KnowledgeDocument> findByScenicAreaIdOrderByCreatedAtDesc(Long scenicAreaId);

    long countByStatus(String status);
}
