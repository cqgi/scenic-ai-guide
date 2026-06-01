package com.scenic.ai.route;

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

@Entity
@Table(name = "route_plan")
public class RoutePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenic_area_id", nullable = false)
    private ScenicArea scenicArea;

    @Column(nullable = false)
    private String name;

    @Column(name = "interest_tags", nullable = false)
    private String interestTags;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "spot_ids", nullable = false)
    private String spotIds;

    @Column(name = "guide_text", nullable = false)
    private String guideText;

    @Column(nullable = false)
    private boolean enabled;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInterestTags() {
        return interestTags;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public String getSpotIds() {
        return spotIds;
    }

    public String getGuideText() {
        return guideText;
    }
}
