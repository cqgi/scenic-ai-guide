package com.scenic.ai.rag.model;

import com.scenic.ai.auth.AdminUser;
import com.scenic.ai.scenic.ScenicArea;
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

@Entity
@Table(name = "knowledge_document")
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenic_area_id", nullable = false)
    private ScenicArea scenicArea;

    @Column(nullable = false)
    private String title;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "doc_type", nullable = false)
    private String docType;

    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AdminUser createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected KnowledgeDocument() {
    }

    public KnowledgeDocument(ScenicArea scenicArea, String title, String fileName, String docType, AdminUser createdBy) {
        this.scenicArea = scenicArea;
        this.title = title;
        this.fileName = fileName;
        this.docType = docType;
        this.status = "active";
        this.createdBy = createdBy;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public ScenicArea getScenicArea() {
        return scenicArea;
    }

    public String getTitle() {
        return title;
    }

    public String getDocType() {
        return docType;
    }

    public String getStatus() {
        return status;
    }
}
