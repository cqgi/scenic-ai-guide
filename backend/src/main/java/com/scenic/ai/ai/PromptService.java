package com.scenic.ai.ai;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PromptService {

    public String scenicQaSystemPrompt() {
        return """
                你是景区数字人导游。回答必须亲切、专业、简洁。
                只能依据给定知识资料回答，不要编造票价、天气、人流、开放状态等实时信息。
                如果资料不足，要明确说明当前知识库没有依据。
                """;
    }

    public String scenicQaUserPrompt(String query, String context) {
        return """
                游客问题：
                %s

                可引用知识资料：
                %s

                请用中文回答，控制在 120 字以内。
                """.formatted(query, context);
    }

    public String routeSystemPrompt() {
        return """
                你是景区路线推荐助手。你要基于给定路线模板和景点信息，输出适合游客兴趣的推荐理由。
                不要引入模板外景点，不要承诺实时排队、天气或票务信息。
                """;
    }

    public String routeUserPrompt(String interests, Integer durationMinutes, String routeSummary) {
        return """
                游客兴趣：%s
                期望时长：%s 分钟

                候选路线：
                %s

                请输出 80 字以内的中文推荐理由。
                """.formatted(interests, durationMinutes == null ? "未指定" : durationMinutes, routeSummary);
    }

    public String rejectionAnswer(String reason) {
        return "抱歉，当前知识库还没有足够依据回答这个问题。" + (reason == null || reason.isBlank() ? "" : "原因：" + reason) + " 我不会编造实时票价、天气、人流或开放状态。";
    }

    public String smalltalkAnswer() {
        return "你好，我是你的景区 AI 导游小栖。你可以问我景点讲解、拍照推荐或游览路线。";
    }

    public String routeFallbackReason(List<String> interests) {
        if (interests == null || interests.isEmpty()) {
            return "这条路线覆盖景区核心节点，适合第一次游览。";
        }
        return "这条路线更贴合你的兴趣：" + String.join("、", interests) + "，游览节奏也比较完整。";
    }
}
