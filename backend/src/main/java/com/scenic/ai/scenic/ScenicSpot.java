package com.scenic.ai.scenic;

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
@Table(name = "scenic_spot")
public class ScenicSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenic_area_id", nullable = false)
    private ScenicArea scenicArea;

    @Column(nullable = false)
    private String name;

    private String alias;

    @Column(nullable = false)
    private String summary;

    private String tags;

    @Column(name = "recommended_minutes", nullable = false)
    private Integer recommendedMinutes;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private boolean enabled;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public String getSummary() {
        return summary;
    }

    public String getTags() {
        return tags;
    }

    public Integer getRecommendedMinutes() {
        return recommendedMinutes;
    }
}
