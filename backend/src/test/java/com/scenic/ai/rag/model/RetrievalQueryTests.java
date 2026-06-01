package com.scenic.ai.rag.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RetrievalQueryTests {

    @Test
    void normalizesBlankInputsAndDefaultLimits() {
        RetrievalQuery query = new RetrievalQuery("  红叶谷  ", null, -1, 0);

        assertThat(query.query()).isEqualTo("红叶谷");
        assertThat(query.interests()).isEmpty();
        assertThat(query.topK()).isEqualTo(10);
        assertThat(query.topN()).isEqualTo(6);
    }
}
