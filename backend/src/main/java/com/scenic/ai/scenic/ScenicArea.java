package com.scenic.ai.scenic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "scenic_area")
public class ScenicArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String summary;

    private String location;

    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(nullable = false)
    private boolean enabled;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public String getLocation() {
        return location;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
