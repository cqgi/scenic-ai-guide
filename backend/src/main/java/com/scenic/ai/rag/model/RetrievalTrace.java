package com.scenic.ai.rag.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "retrieval_trace")
public class RetrievalTrace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interaction_id")
    private Long interactionId;

    @Column(nullable = false)
    private String query;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bm25_candidates", nullable = false, columnDefinition = "jsonb")
    private String bm25Candidates;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keyword_candidates", nullable = false, columnDefinition = "jsonb")
    private String keywordCandidates;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vector_candidates", nullable = false, columnDefinition = "jsonb")
    private String vectorCandidates;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "final_candidates", nullable = false, columnDefinition = "jsonb")
    private String finalCandidates;

    @Column(nullable = false)
    private boolean rejected;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected RetrievalTrace() {
    }

    public RetrievalTrace(String query, String bm25Candidates, String keywordCandidates, String vectorCandidates,
                          String finalCandidates, boolean rejected, String rejectReason) {
        this.query = query;
        this.bm25Candidates = bm25Candidates;
        this.keywordCandidates = keywordCandidates;
        this.vectorCandidates = vectorCandidates;
        this.finalCandidates = finalCandidates;
        this.rejected = rejected;
        this.rejectReason = rejectReason;
        this.createdAt = OffsetDateTime.now();
    }
}
