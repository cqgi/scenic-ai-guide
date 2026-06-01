package com.scenic.ai.ai;

public enum Emotion {
    NEUTRAL("neutral"),
    HAPPY("happy"),
    SORRY("sorry"),
    THINKING("thinking");

    private final String code;

    Emotion(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
