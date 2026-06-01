package com.scenic.ai.interaction;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {

    private final InteractionLogRepository interactionLogRepository;
    private final FeedbackRepository feedbackRepository;

    public FeedbackService(InteractionLogRepository interactionLogRepository, FeedbackRepository feedbackRepository) {
        this.interactionLogRepository = interactionLogRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Transactional
    public FeedbackResponse submit(FeedbackRequest request) {
        if (request.score() < 1 || request.score() > 5) {
            throw new IllegalArgumentException("评分必须在 1 到 5 之间");
        }
        InteractionLog interaction = interactionLogRepository.findByIdAndSessionSessionToken(request.interactionId(), request.sessionToken())
                .orElseThrow(() -> new IllegalArgumentException("交互记录不存在"));
        Feedback feedback = feedbackRepository.save(new Feedback(
                interaction,
                request.score(),
                request.comment(),
                sentiment(request.score())
        ));
        interaction.setSatisfaction(request.score());
        return new FeedbackResponse(feedback.getId(), interaction.getId(), request.score());
    }

    private String sentiment(int score) {
        if (score >= 4) {
            return "positive";
        }
        if (score <= 2) {
            return "negative";
        }
        return "neutral";
    }

    public record FeedbackRequest(String sessionToken, Long interactionId, int score, String comment) {
    }

    public record FeedbackResponse(Long feedbackId, Long interactionId, int score) {
    }
}
