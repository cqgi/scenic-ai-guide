package com.scenic.ai.ai;

import java.util.List;

public interface TextToSpeechClient {
    SynthesisResult synthesize(String text);

    record SynthesisResult(String audioUrl, List<MouthCue> mouthCues, boolean success, String errorMessage) {
        public SynthesisResult {
            mouthCues = mouthCues == null ? List.of() : List.copyOf(mouthCues);
        }
    }

    record MouthCue(double start, double end, String value) {
    }
}
