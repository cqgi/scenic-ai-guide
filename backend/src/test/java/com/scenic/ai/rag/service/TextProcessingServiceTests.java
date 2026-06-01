package com.scenic.ai.rag.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextProcessingServiceTests {

    private final TextProcessingService service = new TextProcessingService();

    @Test
    void cleanAndSplitChunksKeepsParagraphBoundaries() {
        String text = " 第一段介绍红叶谷。\r\n\r\n\r\n第二段介绍步道和观景台。 ";

        List<String> chunks = service.splitChunks(text);

        assertThat(chunks).containsExactly("第一段介绍红叶谷。\n\n第二段介绍步道和观景台。");
    }

    @Test
    void tokenizeFiltersStopWordsAndAddsChineseBigrams() {
        List<String> tokens = service.tokenize("介绍一下红叶谷和佛教文化");

        assertThat(tokens).contains("红叶谷", "红叶", "叶谷", "佛教", "文化");
        assertThat(tokens).doesNotContain("介绍", "一下");
    }

    @Test
    void extractTermsFindsYearsFacilitiesAndPeople() {
        TextProcessingService.ExtractedTerms terms = service.extractTerms(
                "明代古寺附近有观景台，玄奘法师相关故事常被讲解。",
                List.of()
        );

        assertThat(terms.keywords()).contains("明代", "观景台", "玄奘法师");
    }
}
