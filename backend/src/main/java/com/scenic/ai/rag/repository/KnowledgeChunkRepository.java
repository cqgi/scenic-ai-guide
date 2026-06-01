package com.scenic.ai.rag.repository;

import com.scenic.ai.rag.model.KnowledgeChunk;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

    List<KnowledgeChunk> findByDocumentIdOrderByIdAsc(Long documentId);

    List<KnowledgeChunk> findByEnabledTrue();

    List<KnowledgeChunk> findByIdIn(Collection<Long> ids);

    @Query(value = """
            SELECT kc.*, ts_rank_cd(to_tsvector('simple', kc.search_text), to_tsquery('simple', :query)) AS rank
            FROM knowledge_chunk kc
            WHERE kc.enabled = true
              AND to_tsvector('simple', kc.search_text) @@ to_tsquery('simple', :query)
            ORDER BY rank DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeChunk> searchByFullText(@Param("query") String query, @Param("limit") int limit);

    @Query(value = """
            SELECT COALESCE(ts_rank_cd(to_tsvector('simple', kc.search_text), to_tsquery('simple', :query)), 0)
            FROM knowledge_chunk kc
            WHERE kc.id = :chunkId
            """, nativeQuery = true)
    double bm25Score(@Param("chunkId") Long chunkId, @Param("query") String query);
}
