package com.scenic.ai.ai;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        int timeoutSeconds,
        String ttsModel,
        String ttsVoice,
        String asrModel,
        Path audioStoragePath,
        String audioPublicPath
) {
    public AiProperties {
        if (timeoutSeconds <= 0) {
            timeoutSeconds = 20;
        }
        if (ttsModel == null || ttsModel.isBlank()) {
            ttsModel = "tts-1";
        }
        if (ttsVoice == null || ttsVoice.isBlank()) {
            ttsVoice = "alloy";
        }
        if (asrModel == null || asrModel.isBlank()) {
            asrModel = "whisper-1";
        }
        if (audioStoragePath == null) {
            audioStoragePath = Path.of("target/generated-audio");
        }
        if (audioPublicPath == null || audioPublicPath.isBlank()) {
            audioPublicPath = "/generated-audio";
        }
    }
}
