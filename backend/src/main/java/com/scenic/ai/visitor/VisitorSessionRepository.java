package com.scenic.ai.visitor;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorSessionRepository extends JpaRepository<VisitorSession, Long> {
    Optional<VisitorSession> findBySessionToken(String sessionToken);

    @EntityGraph(attributePaths = "scenicArea")
    Optional<VisitorSession> findWithScenicAreaBySessionToken(String sessionToken);
}
