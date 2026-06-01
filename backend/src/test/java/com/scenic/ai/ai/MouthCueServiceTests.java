package com.scenic.ai.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MouthCueServiceTests {

    private final MouthCueService service = new MouthCueService();

    @Test
    void returnsEmptyCuesForBlankText() {
        assertThat(service.approximate("")).isEmpty();
    }

    @Test
    void returnsApproximateTimelineForText() {
        assertThat(service.approximate("红叶谷适合拍照")).isNotEmpty();
    }
}
