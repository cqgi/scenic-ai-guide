package com.scenic.ai.visitor;

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
@Table(name = "visitor_session")
public class VisitorSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenic_area_id", nullable = false)
    private ScenicArea scenicArea;

    @Column(name = "session_token", nullable = false, unique = true)
    private String sessionToken;

    private String interests;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_active_at", nullable = false)
    private OffsetDateTime lastActiveAt;

    protected VisitorSession() {
    }

    public VisitorSession(ScenicArea scenicArea, String sessionToken, String interests, String deviceType) {
        this.scenicArea = scenicArea;
        this.sessionToken = sessionToken;
        this.interests = interests;
        this.deviceType = deviceType;
        this.createdAt = OffsetDateTime.now();
        this.lastActiveAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getInterests() {
        return interests;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public ScenicArea getScenicArea() {
        return scenicArea;
    }

    public void touch() {
        this.lastActiveAt = OffsetDateTime.now();
    }
}
