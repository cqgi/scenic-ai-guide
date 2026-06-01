package com.scenic.ai.ai;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MouthCueService {

    private static final String[] VALUES = {"A", "B", "C", "D", "E", "F"};

    public List<TextToSpeechClient.MouthCue> approximate(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<TextToSpeechClient.MouthCue> cues = new ArrayList<>();
        int units = Math.min(Math.max(text.length() / 3, 1), 80);
        double cursor = 0.0;
        for (int i = 0; i < units; i++) {
            double duration = 0.12;
            cues.add(new TextToSpeechClient.MouthCue(round(cursor), round(cursor + duration), VALUES[i % VALUES.length]));
            cursor += duration;
        }
        return cues;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
