package com.scenic.ai.visitor;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorSessionRepository extends JpaRepository<VisitorSession, Long> {
}
