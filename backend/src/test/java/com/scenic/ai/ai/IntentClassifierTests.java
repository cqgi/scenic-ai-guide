package com.scenic.ai.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IntentClassifierTests {

    private final IntentClassifier classifier = new IntentClassifier();

    @Test
    void classifiesCoreVisitorIntents() {
        assertThat(classifier.classify("帮我推荐一条历史文化路线")).isEqualTo(Intent.ROUTE_RECOMMENDATION);
        assertThat(classifier.classify("你好你是谁")).isEqualTo(Intent.SMALLTALK);
        assertThat(classifier.classify("红叶谷适合拍照吗")).isEqualTo(Intent.SCENIC_QA);
        assertThat(classifier.classify("   ")).isEqualTo(Intent.UNKNOWN);
    }
}
