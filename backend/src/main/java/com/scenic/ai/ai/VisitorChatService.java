package com.scenic.ai.ai;

import com.scenic.ai.interaction.InteractionLog;
import com.scenic.ai.interaction.InteractionLogRepository;
import com.scenic.ai.rag.model.RetrievalQuery;
import com.scenic.ai.rag.model.RetrievalResult;
import com.scenic.ai.rag.repository.RetrievalTraceRepository;
import com.scenic.ai.rag.service.HybridRetrievalService;
import com.scenic.ai.route.RouteRecommendationService;
import com.scenic.ai.visitor.VisitorSession;
import com.scenic.ai.visitor.VisitorSessionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class VisitorChatService {

    private final VisitorSessionRepository visitorSessionRepository;
    private final HybridRetrievalService hybridRetrievalService;
    private final RagContextAssembler ragContextAssembler;
    private final IntentClassifier intentClassifier;
    private final PromptService promptService;
    private final LlmClient llmClient;
    private final EmotionAnalyzer emotionAnalyzer;
    private final TextToSpeechClient textToSpeechClient;
    private final InteractionLogRepository interactionLogRepository;
    private final RetrievalTraceRepository retrievalTraceRepository;
    private final RouteRecommendationService routeRecommendationService;
    private final TransactionTemplate transactionTemplate;

    public VisitorChatService(
            VisitorSessionRepository visitorSessionRepository,
            HybridRetrievalService hybridRetrievalService,
            RagContextAssembler ragContextAssembler,
            IntentClassifier intentClassifier,
            PromptService promptService,
            LlmClient llmClient,
            EmotionAnalyzer emotionAnalyzer,
            TextToSpeechClient textToSpeechClient,
            InteractionLogRepository interactionLogRepository,
            RetrievalTraceRepository retrievalTraceRepository,
            RouteRecommendationService routeRecommendationService,
            TransactionTemplate transactionTemplate
    ) {
        this.visitorSessionRepository = visitorSessionRepository;
        this.hybridRetrievalService = hybridRetrievalService;
        this.ragContextAssembler = ragContextAssembler;
        this.intentClassifier = intentClassifier;
        this.promptService = promptService;
        this.llmClient = llmClient;
        this.emotionAnalyzer = emotionAnalyzer;
        this.textToSpeechClient = textToSpeechClient;
        this.interactionLogRepository = interactionLogRepository;
        this.retrievalTraceRepository = retrievalTraceRepository;
        this.routeRecommendationService = routeRecommendationService;
        this.transactionTemplate = transactionTemplate;
    }

    public ChatResponse chatText(ChatRequest request) {
        return answer(request, "text", null);
    }

    public ChatResponse chatVoice(ChatRequest request, String asrText) {
        return answer(request.withQuery(asrText), "voice", asrText);
    }

    private ChatResponse answer(ChatRequest request, String inputType, String asrText) {
        long start = System.currentTimeMillis();
        VisitorSession session = visitorSessionRepository.findBySessionToken(request.sessionToken())
                .orElseThrow(() -> new IllegalArgumentException("游客 session 不存在或已过期"));

        Intent intent = intentClassifier.classify(request.query());
        RetrievalResult retrieval = new RetrievalResult(request.query(), List.of(), List.of(), List.of(), List.of(), true, "非景区知识问答");
        List<RagContextAssembler.SourceChunk> sourceChunks = List.of();
        String answer;
        boolean rejected = false;
        boolean fallback = false;

        if (intent == Intent.SMALLTALK) {
            answer = promptService.smalltalkAnswer();
        } else if (intent == Intent.ROUTE_RECOMMENDATION) {
            RouteRecommendationService.RouteRecommendation recommendation = routeRecommendationService.recommend(
                    new RouteRecommendationService.RouteRecommendRequest(request.sessionToken(), request.interests(), null)
            );
            answer = routeAnswer(recommendation);
        } else {
            retrieval = hybridRetrievalService.search(new RetrievalQuery(request.query(), request.interests(), 10, 6));
            sourceChunks = ragContextAssembler.sourceChunks(retrieval);
            if (retrieval.rejected()) {
                rejected = true;
                answer = promptService.rejectionAnswer(retrieval.rejectReason());
            } else {
                String context = ragContextAssembler.buildContext(sourceChunks);
                LlmClient.LlmResponse llmResponse = llmClient.chat(
                        promptService.scenicQaSystemPrompt(),
                        promptService.scenicQaUserPrompt(request.query(), context)
                );
                answer = llmResponse.content();
                fallback = llmResponse.fallback();
            }
        }

        Emotion emotion = emotionAnalyzer.analyze(answer, rejected, fallback);
        TextToSpeechClient.SynthesisResult speech = textToSpeechClient.synthesize(answer);
        int latencyMs = Math.toIntExact(Math.min(System.currentTimeMillis() - start, Integer.MAX_VALUE));
        InteractionLog log = saveInteraction(request, inputType, asrText, intent, answer, emotion, sourceChunks, latencyMs,
                retrieval.traceId());
        return new ChatResponse(
                log.getId(),
                intent.code(),
                answer,
                emotion.code(),
                sourceChunks,
                speech.audioUrl(),
                speech.mouthCues(),
                rejected,
                asrText
        );
    }

    private InteractionLog saveInteraction(ChatRequest request, String inputType, String asrText, Intent intent,
                                           String answer, Emotion emotion,
                                           List<RagContextAssembler.SourceChunk> sourceChunks, int latencyMs,
                                           Long retrievalTraceId) {
        return transactionTemplate.execute(status -> {
            VisitorSession session = visitorSessionRepository.findBySessionToken(request.sessionToken())
                    .orElseThrow(() -> new IllegalArgumentException("游客 session 不存在或已过期"));
            session.touch();
            InteractionLog log = interactionLogRepository.save(new InteractionLog(
                    session,
                    inputType,
                    request.query(),
                    asrText,
                    intent.code(),
                    answer,
                    emotion.code(),
                    ragContextAssembler.sourceChunkIds(sourceChunks),
                    latencyMs
            ));
            if (retrievalTraceId != null) {
                retrievalTraceRepository.findById(retrievalTraceId).ifPresent(trace -> {
                    trace.setInteractionId(log.getId());
                    retrievalTraceRepository.save(trace);
                });
            }
            return log;
        });
    }

    private String routeAnswer(RouteRecommendationService.RouteRecommendation recommendation) {
        String spots = recommendation.spots().isEmpty()
                ? "暂无可展示景点"
                : String.join("、", recommendation.spots().stream().map(RouteRecommendationService.RouteSpot::name).toList());
        return """
                推荐路线：%s
                预计用时：%d 分钟
                途经景点：%s
                推荐理由：%s
                讲解词：%s
                """.formatted(
                recommendation.name(),
                recommendation.durationMinutes(),
                spots,
                recommendation.reason(),
                recommendation.guideText()
        ).trim();
    }

    public record ChatRequest(String sessionToken, String query, List<String> interests) {
        public ChatRequest {
            interests = interests == null ? List.of() : List.copyOf(interests);
        }

        ChatRequest withQuery(String newQuery) {
            return new ChatRequest(sessionToken, newQuery, interests);
        }
    }

    public record ChatResponse(
            Long interactionId,
            String intent,
            String answer,
            String emotion,
            List<RagContextAssembler.SourceChunk> sourceChunks,
            String audioUrl,
            List<TextToSpeechClient.MouthCue> mouthCues,
            boolean rejected,
            String asrText
    ) {
    }
}
