package com.scenic.ai.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.scenic.ai.interaction.InteractionLog;
import com.scenic.ai.interaction.InteractionLogRepository;
import com.scenic.ai.rag.model.RetrievalResult;
import com.scenic.ai.rag.service.HybridRetrievalService;
import com.scenic.ai.route.RouteRecommendationService;
import com.scenic.ai.visitor.VisitorSession;
import com.scenic.ai.visitor.VisitorSessionRepository;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

class VisitorChatServiceTests {

    @Test
    void rejectedRagDoesNotCallLlmAndStillWritesLog() {
        CapturingLlmClient llm = new CapturingLlmClient();
        List<InteractionLog> logs = new ArrayList<>();
        VisitorChatService service = new VisitorChatService(
                sessionRepository(),
                new RejectedRetrievalService(),
                new RagContextAssembler(),
                new IntentClassifier(),
                new PromptService(),
                llm,
                new EmotionAnalyzer(),
                text -> new TextToSpeechClient.SynthesisResult(null, List.of(), false, "fallback"),
                interactionRepository(logs),
                routeRecommendationService(),
                transactionTemplate()
        );

        VisitorChatService.ChatResponse response = service.chatText(new VisitorChatService.ChatRequest("token", "今天红叶谷人多吗", List.of()));

        assertThat(response.rejected()).isTrue();
        assertThat(response.answer()).contains("当前知识库还没有足够依据");
        assertThat(llm.called).isFalse();
        assertThat(logs).hasSize(1);
    }

    @Test
    void routeIntentUsesRouteRecommendationInsteadOfRag() {
        List<InteractionLog> logs = new ArrayList<>();
        VisitorChatService service = new VisitorChatService(
                sessionRepository(),
                new FailingRetrievalService(),
                new RagContextAssembler(),
                new IntentClassifier(),
                new PromptService(),
                new CapturingLlmClient(),
                new EmotionAnalyzer(),
                text -> new TextToSpeechClient.SynthesisResult(null, List.of(), false, "fallback"),
                interactionRepository(logs),
                routeRecommendationService(),
                transactionTemplate()
        );

        VisitorChatService.ChatResponse response = service.chatText(new VisitorChatService.ChatRequest("token", "帮我推荐一条拍照路线", List.of("photo")));

        assertThat(response.intent()).isEqualTo(Intent.ROUTE_RECOMMENDATION.code());
        assertThat(response.answer()).contains("推荐路线：自然风光轻松线");
        assertThat(response.sourceChunks()).isEmpty();
        assertThat(logs).hasSize(1);
    }

    private static class CapturingLlmClient implements LlmClient {
        private boolean called;

        @Override
        public LlmResponse chat(String systemPrompt, String userPrompt) {
            called = true;
            return new LlmResponse("llm answer", false, null);
        }
    }

    private static class RejectedRetrievalService extends HybridRetrievalService {
        RejectedRetrievalService() {
            super(null, null, null, null, null, null, null);
        }

        @Override
        public RetrievalResult search(com.scenic.ai.rag.model.RetrievalQuery query) {
            return new RetrievalResult(query.query(), List.of(), List.of(), List.of(), List.of(), true, "实时信息");
        }
    }

    private static class FailingRetrievalService extends HybridRetrievalService {
        FailingRetrievalService() {
            super(null, null, null, null, null, null, null);
        }

        @Override
        public RetrievalResult search(com.scenic.ai.rag.model.RetrievalQuery query) {
            throw new AssertionError("route intent should not call RAG search");
        }
    }

    private RouteRecommendationService routeRecommendationService() {
        return new RouteRecommendationService(null, null, null, new PromptService(), new CapturingLlmClient()) {
            @Override
            public RouteRecommendation recommend(RouteRecommendRequest request) {
                return new RouteRecommendation(
                        1L,
                        "自然风光轻松线",
                        100,
                        List.of(new RouteSpot(2L, "明镜湖", "适合拍照", 20)),
                        "适合拍照和轻松游览。",
                        "从游客中心进入后游览明镜湖。"
                );
            }
        };
    }

    private TransactionTemplate transactionTemplate() {
        return new TransactionTemplate() {
            @Override
            public <T> T execute(org.springframework.transaction.support.TransactionCallback<T> action) {
                return action.doInTransaction(null);
            }
        };
    }

    private VisitorSessionRepository sessionRepository() {
        return (VisitorSessionRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{VisitorSessionRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("findBySessionToken")) {
                        return Optional.of(new VisitorSession(null, (String) args[0], "", "test"));
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private InteractionLogRepository interactionRepository(List<InteractionLog> saved) {
        return (InteractionLogRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{InteractionLogRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("save")) {
                        saved.add((InteractionLog) args[0]);
                        return args[0];
                    }
                    throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
