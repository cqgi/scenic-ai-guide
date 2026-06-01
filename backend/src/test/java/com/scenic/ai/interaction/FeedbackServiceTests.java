package com.scenic.ai.interaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.scenic.ai.visitor.VisitorSession;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FeedbackServiceTests {

    @Test
    void savesFeedbackAndUpdatesInteractionSatisfaction() {
        InteractionLog interaction = new InteractionLog(new VisitorSession(null, "token", "", "test"),
                "text", "q", null, "scenic_qa", "a", "happy", "1", 10);
        List<Feedback> feedbacks = new ArrayList<>();
        FeedbackService service = new FeedbackService(interactionRepository(interaction), feedbackRepository(feedbacks));

        FeedbackService.FeedbackResponse response = service.submit(new FeedbackService.FeedbackRequest("token", 1L, 5, "好"));

        assertThat(response.score()).isEqualTo(5);
        assertThat(interaction.getSatisfaction()).isEqualTo(5);
        assertThat(feedbacks).hasSize(1);
    }

    @Test
    void rejectsFeedbackFromDifferentSession() {
        FeedbackService service = new FeedbackService(emptyInteractionRepository(), feedbackRepository(new ArrayList<>()));

        assertThatThrownBy(() -> service.submit(new FeedbackService.FeedbackRequest("other-token", 1L, 5, "好")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("交互记录不存在");
    }

    private InteractionLogRepository interactionRepository(InteractionLog interaction) {
        return (InteractionLogRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{InteractionLogRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("findByIdAndSessionSessionToken")) {
                        return Optional.of(interaction);
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private InteractionLogRepository emptyInteractionRepository() {
        return (InteractionLogRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{InteractionLogRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("findByIdAndSessionSessionToken")) {
                        return Optional.empty();
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private FeedbackRepository feedbackRepository(List<Feedback> saved) {
        return (FeedbackRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{FeedbackRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("save")) {
                        saved.add((Feedback) args[0]);
                        return args[0];
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
