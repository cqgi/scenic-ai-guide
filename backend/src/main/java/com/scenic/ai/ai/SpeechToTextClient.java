package com.scenic.ai.ai;

public interface SpeechToTextClient {
    TranscriptionResult transcribe(byte[] audioBytes, String fileName, String contentType);

    record TranscriptionResult(String text, boolean success, String errorMessage) {
    }
}
