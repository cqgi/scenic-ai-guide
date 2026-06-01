package com.scenic.ai.ai;

public enum Intent {
    SCENIC_QA("scenic_qa"),
    ROUTE_RECOMMENDATION("route_recommendation"),
    SMALLTALK("smalltalk"),
    UNKNOWN("unknown");

    private final String code;

    Intent(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
