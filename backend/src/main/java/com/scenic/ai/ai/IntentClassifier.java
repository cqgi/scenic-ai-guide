package com.scenic.ai.ai;

import org.springframework.stereotype.Service;

@Service
public class IntentClassifier {

    public Intent classify(String query) {
        String text = query == null ? "" : query.trim();
        if (text.isBlank()) {
            return Intent.UNKNOWN;
        }
        if (containsAny(text, "路线", "怎么走", "游览顺序", "推荐线", "行程", "几个小时", "半日游", "亲子游")) {
            return Intent.ROUTE_RECOMMENDATION;
        }
        if (containsAny(text, "你好", "您好", "谢谢", "你是谁", "早上好", "下午好")) {
            return Intent.SMALLTALK;
        }
        if (containsAny(text, "介绍", "讲讲", "讲解", "适合", "景点", "历史", "文化", "拍照", "红叶", "古寺", "明镜湖", "碑刻", "游客中心", "观景台")) {
            return Intent.SCENIC_QA;
        }
        return Intent.SCENIC_QA;
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }
}
