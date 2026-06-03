package com.scenic.ai.rag.model;

import com.scenic.ai.scenic.ScenicSpot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "knowledge_chunk")
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private KnowledgeDocument document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenic_spot_id")
    private ScenicSpot scenicSpot;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private String summary;
    private String keywords;
    private String aliases;

    @Column(name = "spot_names")
    private String spotNames;

    private String tags;

    @Column(name = "search_text", nullable = false)
    private String searchText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json", nullable = false, columnDefinition = "jsonb")
    private String metadataJson;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected KnowledgeChunk() {
    }

    public KnowledgeChunk(
            KnowledgeDocument document,
            ScenicSpot scenicSpot,
            String title,
            String content,
            String summary,
            String keywords,
            String aliases,
            String spotNames,
            String tags,
            String searchText,
            String metadataJson
    ) {
        this.document = document;
        this.scenicSpot = scenicSpot;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.keywords = keywords;
        this.aliases = aliases;
        this.spotNames = spotNames;
        this.tags = tags;
        this.searchText = searchText;
        this.metadataJson = metadataJson;
        this.enabled = true;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public KnowledgeDocument getDocument() {
        return document;
    }

    public ScenicSpot getScenicSpot() {
        return scenicSpot;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSummary() {
        return summary;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getAliases() {
        return aliases;
    }

    public String getSpotNames() {
        return spotNames;
    }

    public String getTags() {
        return tags;
    }

    public String getSearchText() {
        return searchText;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = OffsetDateTime.now();
    }
}
