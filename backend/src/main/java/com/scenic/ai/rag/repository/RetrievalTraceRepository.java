package com.scenic.ai.rag.repository;

import com.scenic.ai.rag.model.RetrievalTrace;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetrievalTraceRepository extends JpaRepository<RetrievalTrace, Long> {
    Optional<RetrievalTrace> findByInteractionId(Long interactionId);

    long countByRejectedTrue();
}
