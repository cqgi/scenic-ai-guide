package com.scenic.ai.interaction;

import com.scenic.ai.visitor.VisitorSession;
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
@Table(name = "interaction_log")
public class InteractionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private VisitorSession session;

    @Column(name = "input_type", nullable = false)
    private String inputType;

    @Column(name = "user_query", nullable = false)
    private String userQuery;

    @Column(name = "asr_text")
    private String asrText;

    private String intent;
    private String answer;
    private String emotion;

    @Column(name = "source_chunk_ids")
    private String sourceChunkIds;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    private Integer satisfaction;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected InteractionLog() {
    }

    public InteractionLog(VisitorSession session, String inputType, String userQuery, String asrText,
                          String intent, String answer, String emotion, String sourceChunkIds, Integer latencyMs) {
        this.session = session;
        this.inputType = inputType;
        this.userQuery = userQuery;
        this.asrText = asrText;
        this.intent = intent;
        this.answer = answer;
        this.emotion = emotion;
        this.sourceChunkIds = sourceChunkIds;
        this.latencyMs = latencyMs;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public VisitorSession getSession() {
        return session;
    }

    public String getUserQuery() {
        return userQuery;
    }

    public String getInputType() {
        return inputType;
    }

    public String getAsrText() {
        return asrText;
    }

    public String getIntent() {
        return intent;
    }

    public String getAnswer() {
        return answer;
    }

    public String getEmotion() {
        return emotion;
    }

    public String getSourceChunkIds() {
        return sourceChunkIds;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public Integer getSatisfaction() {
        return satisfaction;
    }

    public void setSatisfaction(Integer satisfaction) {
        this.satisfaction = satisfaction;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
