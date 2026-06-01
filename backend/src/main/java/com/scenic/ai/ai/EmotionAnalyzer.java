package com.scenic.ai.ai;

import org.springframework.stereotype.Service;

@Service
public class EmotionAnalyzer {

    public Emotion analyze(String answer, boolean rejected, boolean fallback) {
        if (rejected || fallback) {
            return Emotion.SORRY;
        }
        String text = answer == null ? "" : answer;
        if (text.contains("推荐") || text.contains("适合") || text.contains("欢迎")) {
            return Emotion.HAPPY;
        }
        if (text.contains("稍等") || text.contains("建议")) {
            return Emotion.THINKING;
        }
        return Emotion.NEUTRAL;
    }
}
