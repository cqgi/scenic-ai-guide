package com.scenic.ai.ai;

public interface LlmClient {
    LlmResponse chat(String systemPrompt, String userPrompt);

    record LlmResponse(String content, boolean fallback, String errorMessage) {
    }
}
