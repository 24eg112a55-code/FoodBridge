package com.communityfoodsharing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "distributions")
public class Distribution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "donation_id", unique = true) private FoodDonation donation;
    @Column(nullable = false, length = 300) private String distributionLocation;
    @Column(nullable = false, length = 160) private String organizationName;
    @Column(nullable = false) private int peopleServed;
    @Column(length = 1200) private String distributionNotes;
    @Column(nullable = false) private LocalDateTime distributedAt;
    private LocalDateTime completedAt;
    public Long getId() { return id; }
    public FoodDonation getDonation() { return donation; }
    public void setDonation(FoodDonation value) { donation = value; }
    public String getDistributionLocation() { return distributionLocation; }
    public void setDistributionLocation(String value) { distributionLocation = value; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String value) { organizationName = value; }
    public int getPeopleServed() { return peopleServed; }
    public void setPeopleServed(int value) { peopleServed = value; }
    public String getDistributionNotes() { return distributionNotes; }
    public void setDistributionNotes(String value) { distributionNotes = value; }
    public LocalDateTime getDistributedAt() { return distributedAt; }
    public void setDistributedAt(LocalDateTime value) { distributedAt = value; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime value) { completedAt = value; }
}