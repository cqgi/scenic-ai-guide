package com.scenic.ai.rag.repository;

import com.scenic.ai.rag.model.KnowledgeChunk;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgeChunkRepository extends JpaRepository<KnowledgeChunk, Long> {

    @EntityGraph(attributePaths = {"document", "scenicSpot"})
    List<KnowledgeChunk> findByDocumentIdOrderByIdAsc(Long documentId);

    List<KnowledgeChunk> findByEnabledTrue();

    @EntityGraph(attributePaths = {"document", "scenicSpot"})
    List<KnowledgeChunk> findByIdIn(Collection<Long> ids);

    @EntityGraph(attributePaths = {"document", "scenicSpot"})
    @Query("SELECT chunk FROM KnowledgeChunk chunk WHERE chunk.id = :id")
    Optional<KnowledgeChunk> findByIdWithDocumentAndScenicSpot(@Param("id") Long id);

    long countByDocumentId(Long documentId);

    long countByDocumentIdAndEnabledTrue(Long documentId);

    @Query(value = """
            SELECT DISTINCT kc.*
            FROM knowledge_chunk kc
            WHERE kc.enabled = true
              AND (
                    kc.keywords ILIKE CONCAT('%', :term, '%')
                 OR kc.aliases ILIKE CONCAT('%', :term, '%')
                 OR kc.spot_names ILIKE CONCAT('%', :term, '%')
                 OR kc.title ILIKE CONCAT('%', :term, '%')
                 OR kc.search_text ILIKE CONCAT('%', :term, '%')
              )
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeChunk> findKeywordCandidates(@Param("term") String term, @Param("limit") int limit);

    @Query(value = """
            SELECT kc.*, ts_rank_cd(to_tsvector('simple', kc.search_text), websearch_to_tsquery('simple', :query)) AS rank
            FROM knowledge_chunk kc
            WHERE kc.enabled = true
              AND to_tsvector('simple', kc.search_text) @@ websearch_to_tsquery('simple', :query)
            ORDER BY rank DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<KnowledgeChunk> searchByFullText(@Param("query") String query, @Param("limit") int limit);

    @Query(value = """
            SELECT COALESCE(ts_rank_cd(to_tsvector('simple', kc.search_text), websearch_to_tsquery('simple', :query)), 0)
            FROM knowledge_chunk kc
            WHERE kc.id = :chunkId
            """, nativeQuery = true)
    double bm25Score(@Param("chunkId") Long chunkId, @Param("query") String query);
}
