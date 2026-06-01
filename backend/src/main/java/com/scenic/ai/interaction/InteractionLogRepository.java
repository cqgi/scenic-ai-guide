package com.scenic.ai.interaction;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteractionLogRepository extends JpaRepository<InteractionLog, Long> {
    Optional<InteractionLog> findByIdAndSessionSessionToken(Long id, String sessionToken);
}
