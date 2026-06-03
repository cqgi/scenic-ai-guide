package com.scenic.ai.digitalhuman;

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
@Table(name = "digital_human_profile")
public class DigitalHumanProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenic_area_id", nullable = false)
    private ScenicArea scenicArea;

    @Column(nullable = false)
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "model_url")
    private String modelUrl;

    @Column(name = "voice_code")
    private String voiceCode;

    @Column(name = "clothing_style")
    private String clothingStyle;

    @Column(name = "welcome_text", nullable = false)
    private String welcomeText;

    @Column(name = "persona_prompt", nullable = false)
    private String personaPrompt;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected DigitalHumanProfile() {
    }

    public DigitalHumanProfile(ScenicArea scenicArea, String name, String welcomeText, String personaPrompt) {
        this.scenicArea = scenicArea;
        this.name = name;
        this.welcomeText = welcomeText;
        this.personaPrompt = personaPrompt;
        this.enabled = true;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public ScenicArea getScenicArea() {
        return scenicArea;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getModelUrl() {
        return modelUrl;
    }

    public String getVoiceCode() {
        return voiceCode;
    }

    public String getClothingStyle() {
        return clothingStyle;
    }

    public String getWelcomeText() {
        return welcomeText;
    }

    public String getPersonaPrompt() {
        return personaPrompt;
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

    public void update(String name, String avatarUrl, String modelUrl, String voiceCode, String clothingStyle,
                       String welcomeText, String personaPrompt, boolean enabled) {
        this.name = name;
        this.avatarUrl = blankToNull(avatarUrl);
        this.modelUrl = blankToNull(modelUrl);
        this.voiceCode = blankToNull(voiceCode);
        this.clothingStyle = blankToNull(clothingStyle);
        this.welcomeText = welcomeText;
        this.personaPrompt = personaPrompt;
        this.enabled = enabled;
        this.updatedAt = OffsetDateTime.now();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
