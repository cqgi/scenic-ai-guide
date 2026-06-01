package com.scenic.ai.rag.model;

import java.util.ArrayList;
import java.util.List;

public class RetrievalCandidate {
    private Long chunkId;
    private Long documentId;
    private String title;
    private String content;
    private String sourceType;
    private double bm25Score;
    private double keywordScore;
    private double vectorScore;
    private double metadataBoost;
    private double finalScore;
    private List<String> hitReasons = new ArrayList<>();

    public static RetrievalCandidate fromChunk(KnowledgeChunk chunk, String sourceType) {
        RetrievalCandidate candidate = new RetrievalCandidate();
        candidate.chunkId = chunk.getId();
        candidate.documentId = chunk.getDocument().getId();
        candidate.title = chunk.getTitle();
        candidate.content = chunk.getContent();
        candidate.sourceType = sourceType;
        return candidate;
    }

    public RetrievalCandidate copy() {
        RetrievalCandidate candidate = new RetrievalCandidate();
        candidate.chunkId = this.chunkId;
        candidate.documentId = this.documentId;
        candidate.title = this.title;
        candidate.content = this.content;
        candidate.sourceType = this.sourceType;
        candidate.bm25Score = this.bm25Score;
        candidate.keywordScore = this.keywordScore;
        candidate.vectorScore = this.vectorScore;
        candidate.metadataBoost = this.metadataBoost;
        candidate.finalScore = this.finalScore;
        candidate.hitReasons = new ArrayList<>(this.hitReasons);
        return candidate;
    }

    public Long getChunkId() {
        return chunkId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public double getBm25Score() {
        return bm25Score;
    }

    public void setBm25Score(double bm25Score) {
        this.bm25Score = bm25Score;
    }

    public double getKeywordScore() {
        return keywordScore;
    }

    public void setKeywordScore(double keywordScore) {
        this.keywordScore = keywordScore;
    }

    public double getVectorScore() {
        return vectorScore;
    }

    public void setVectorScore(double vectorScore) {
        this.vectorScore = vectorScore;
    }

    public double getMetadataBoost() {
        return metadataBoost;
    }

    public void setMetadataBoost(double metadataBoost) {
        this.metadataBoost = metadataBoost;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public List<String> getHitReasons() {
        return hitReasons;
    }

    public void addHitReason(String reason) {
        if (!hitReasons.contains(reason)) {
            hitReasons.add(reason);
        }
    }
}
