package com.scenic.ai.interaction;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findTopByInteractionIdOrderByCreatedAtDesc(Long interactionId);

    List<Feedback> findTop20ByScoreLessThanEqualOrderByCreatedAtDesc(Integer score);
}
