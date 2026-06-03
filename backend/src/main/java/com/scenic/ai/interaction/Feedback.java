package com.scenic.ai.interaction;

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
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interaction_id", nullable = false)
    private InteractionLog interaction;

    @Column(nullable = false)
    private Integer score;

    private String comment;
    private String sentiment;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Feedback() {
    }

    public Feedback(InteractionLog interaction, Integer score, String comment, String sentiment) {
        this.interaction = interaction;
        this.score = score;
        this.comment = comment;
        this.sentiment = sentiment;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public InteractionLog getInteraction() {
        return interaction;
    }

    public Integer getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }

    public String getSentiment() {
        return sentiment;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
