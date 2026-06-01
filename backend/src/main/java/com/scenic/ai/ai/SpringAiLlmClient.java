package com.scenic.ai.ai;

import java.util.List;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class SpringAiLlmClient implements LlmClient {

    private static final String FALLBACK_ANSWER = "抱歉，我现在暂时无法连接大模型服务。你可以稍后再试，或换一个更具体的景区问题。";

    private final ChatModel chatModel;

    public SpringAiLlmClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public LlmResponse chat(String systemPrompt, String userPrompt) {
        try {
            String content = chatModel.call(new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)
            ))).getResult().getOutput().getText();
            if (content == null || content.isBlank()) {
                return new LlmResponse(FALLBACK_ANSWER, true, "empty chat response");
            }
            return new LlmResponse(content.trim(), false, null);
        } catch (RuntimeException exception) {
            return new LlmResponse(FALLBACK_ANSWER, true, exception.getMessage());
        }
    }
}
