package com.scenic.ai.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class OpenAiCompatibleSpeechToTextClient implements SpeechToTextClient {

    private final AiProperties properties;
    private final RestClient restClient;
    private final String apiKey;

    public OpenAiCompatibleSpeechToTextClient(
            AiProperties properties,
            RestClient.Builder restClientBuilder,
            @Value("${spring.ai.openai.base-url:https://api.openai.com}") String baseUrl,
            @Value("${spring.ai.openai.api-key:dummy-key}") String apiKey
    ) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    public TranscriptionResult transcribe(byte[] audioBytes, String fileName, String contentType) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("dummy-key")) {
            return new TranscriptionResult(null, false, "asr api key is not configured");
        }
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("model", properties.asrModel());
            body.add("file", new NamedByteArrayResource(audioBytes, fileName == null ? "audio.webm" : fileName));
            TranscriptionResponse response = restClient.post()
                    .uri("/v1/audio/transcriptions")
                    .headers(headers -> headers.setBearerAuth(apiKey))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(TranscriptionResponse.class);
            String text = response == null ? null : response.text();
            if (text == null || text.isBlank()) {
                return new TranscriptionResult(null, false, "empty asr response");
            }
            return new TranscriptionResult(text.trim(), true, null);
        } catch (RuntimeException exception) {
            return new TranscriptionResult(null, false, exception.getMessage());
        }
    }

    private record TranscriptionResponse(String text) {
    }

    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }

}
