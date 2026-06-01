package com.scenic.ai.rag.model;

import java.util.List;

public record RetrievalQuery(String query, List<String> interests, int topK, int topN) {
    public RetrievalQuery {
        query = query == null ? "" : query.trim();
        if (topK <= 0) {
            topK = 10;
        }
        if (topN <= 0) {
            topN = 6;
        }
        interests = interests == null ? List.of() : List.copyOf(interests);
    }
}
