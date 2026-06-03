package com.scenic.ai.interaction;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InteractionLogRepository extends JpaRepository<InteractionLog, Long> {
    Optional<InteractionLog> findByIdAndSessionSessionToken(Long id, String sessionToken);

    @EntityGraph(attributePaths = {"session", "session.scenicArea"})
    @Query("""
            SELECT i
            FROM InteractionLog i
            WHERE (:keyword IS NULL OR LOWER(i.userQuery) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(i.answer) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:intent IS NULL OR i.intent = :intent)
              AND (:emotion IS NULL OR i.emotion = :emotion)
              AND (:satisfaction IS NULL OR i.satisfaction = :satisfaction)
            """)
    Page<InteractionLog> searchAdmin(
            @Param("keyword") String keyword,
            @Param("intent") String intent,
            @Param("emotion") String emotion,
            @Param("satisfaction") Integer satisfaction,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"session", "session.scenicArea"})
    Optional<InteractionLog> findWithSessionById(Long id);

    long countByCreatedAtAfter(OffsetDateTime createdAt);

    long countByIntent(String intent);

    @Query("SELECT AVG(i.satisfaction) FROM InteractionLog i WHERE i.satisfaction IS NOT NULL")
    Double averageSatisfaction();

    @Query("SELECT AVG(i.latencyMs) FROM InteractionLog i WHERE i.latencyMs IS NOT NULL")
    Double averageLatencyMs();

    @Query("""
            SELECT i
            FROM InteractionLog i
            WHERE i.createdAt >= :start
            ORDER BY i.createdAt ASC
            """)
    List<InteractionLog> findSince(@Param("start") OffsetDateTime start);

    @Query("""
            SELECT i.userQuery, COUNT(i)
            FROM InteractionLog i
            GROUP BY i.userQuery
            ORDER BY COUNT(i) DESC
            """)
    List<Object[]> hotQuestions(Pageable pageable);
}
