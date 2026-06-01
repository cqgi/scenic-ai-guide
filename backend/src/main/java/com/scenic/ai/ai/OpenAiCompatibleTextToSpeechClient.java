package com.scenic.ai.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenAiCompatibleTextToSpeechClient implements TextToSpeechClient {

    private final AiProperties properties;
    private final MouthCueService mouthCueService;
    private final RestClient restClient;
    private final String apiKey;
    private final String contextPath;

    public OpenAiCompatibleTextToSpeechClient(
            AiProperties properties,
            MouthCueService mouthCueService,
            RestClient.Builder restClientBuilder,
            @Value("${spring.ai.openai.base-url:https://api.openai.com}") String baseUrl,
            @Value("${spring.ai.openai.api-key:dummy-key}") String apiKey,
            @Value("${server.servlet.context-path:}") String contextPath
    ) {
        this.properties = properties;
        this.mouthCueService = mouthCueService;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.contextPath = normalizePath(contextPath);
    }

    @Override
    public SynthesisResult synthesize(String text) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("dummy-key")) {
            return new SynthesisResult(null, mouthCueService.approximate(text), false, "tts api key is not configured");
        }
        try {
            byte[] audio = restClient.post()
                    .uri("/v1/audio/speech")
                    .headers(headers -> headers.setBearerAuth(apiKey))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", properties.ttsModel(),
                            "voice", properties.ttsVoice(),
                            "input", text,
                            "response_format", "mp3"
                    ))
                    .retrieve()
                    .body(byte[].class);
            if (audio == null || audio.length == 0) {
                return new SynthesisResult(null, mouthCueService.approximate(text), false, "empty tts response");
            }
            String publicUrl = saveAudio(audio);
            return new SynthesisResult(publicUrl, mouthCueService.approximate(text), true, null);
        } catch (RuntimeException | IOException exception) {
            return new SynthesisResult(null, mouthCueService.approximate(text), false, exception.getMessage());
        }
    }

    private String saveAudio(byte[] audio) throws IOException {
        String date = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Path directory = properties.audioStoragePath().resolve(date);
        Files.createDirectories(directory);
        String fileName = UUID.randomUUID() + ".mp3";
        Files.write(directory.resolve(fileName), audio);
        return contextPath + normalizePath(properties.audioPublicPath()) + "/" + date + "/" + fileName;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank() || path.equals("/")) {
            return "";
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        return normalized.replaceAll("/+$", "");
    }
}
