package com.scenic.ai.ai;

import com.scenic.ai.rag.model.RetrievalCandidate;
import com.scenic.ai.rag.model.RetrievalResult;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RagContextAssembler {

    private static final int MAX_CONTEXT_CHUNKS = 4;

    public List<SourceChunk> sourceChunks(RetrievalResult result) {
        return result.finalCandidates().stream()
                .limit(MAX_CONTEXT_CHUNKS)
                .map(SourceChunk::from)
                .toList();
    }

    public String buildContext(List<SourceChunk> chunks) {
        if (chunks.isEmpty()) {
            return "无可用知识片段。";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            SourceChunk chunk = chunks.get(i);
            builder.append("资料").append(i + 1)
                    .append(" [chunkId=").append(chunk.chunkId()).append(", title=").append(chunk.title()).append("]\n")
                    .append(chunk.content()).append("\n\n");
        }
        return builder.toString().trim();
    }

    public String sourceChunkIds(List<SourceChunk> chunks) {
        return String.join(",", chunks.stream().map(chunk -> String.valueOf(chunk.chunkId())).toList());
    }

    public record SourceChunk(Long chunkId, Long documentId, String title, String content, double finalScore) {
        static SourceChunk from(RetrievalCandidate candidate) {
            return new SourceChunk(
                    candidate.getChunkId(),
                    candidate.getDocumentId(),
                    candidate.getTitle(),
                    candidate.getContent(),
                    candidate.getFinalScore()
            );
        }
    }
}
