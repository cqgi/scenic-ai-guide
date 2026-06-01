package com.scenic.ai.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.scenic.ai.rag.model.RetrievalCandidate;
import com.scenic.ai.rag.model.RetrievalResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class RagContextAssemblerTests {

    private final RagContextAssembler assembler = new RagContextAssembler();

    @Test
    void keepsOnlyTopFourSourceChunks() {
        RetrievalResult result = new RetrievalResult("q", List.of(), List.of(), List.of(),
                List.of(candidate(1), candidate(2), candidate(3), candidate(4), candidate(5)), false, null);

        List<RagContextAssembler.SourceChunk> chunks = assembler.sourceChunks(result);

        assertThat(chunks).hasSize(4);
        assertThat(assembler.sourceChunkIds(chunks)).isEqualTo("1,2,3,4");
        assertThat(assembler.buildContext(chunks)).contains("chunkId=1", "标题1");
    }

    private RetrievalCandidate candidate(long id) {
        RetrievalCandidate candidate = new RetrievalCandidate();
        candidate.setChunkId(id);
        candidate.setDocumentId(10L);
        candidate.setTitle("标题" + id);
        candidate.setContent("内容" + id);
        candidate.setFinalScore(0.9);
        return candidate;
    }
}
