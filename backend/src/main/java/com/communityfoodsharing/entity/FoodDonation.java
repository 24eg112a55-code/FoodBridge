package com.communityfoodsharing.entity;

import com.communityfoodsharing.enums.DonationStatus;
import com.communityfoodsharing.enums.FoodType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "food_donations", indexes = {@Index(columnList = "status"), @Index(columnList = "createdAt")})
public class FoodDonation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String foodName;
    @Column(nullable = false, length = 1200) private String description;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal quantity;
    @Column(nullable = false, length = 32) private String quantityUnit;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private FoodType foodType;
    @Column(nullable = false) private LocalDateTime expiryTime;
    @Column(nullable = false, length = 300) private String pickupAddress;
    @Column(nullable = false, length = 100) private String city;
    @Column(nullable = false) private LocalDate pickupDate;
    @Column(nullable = false) private LocalTime pickupTime;
    @Column(length = 1200) private String additionalInstructions;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private DonationStatus status = DonationStatus.AVAILABLE;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "donor_id") private User donor;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "volunteer_id") private User volunteer;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate void updateTimestamp() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String value) { foodName = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal value) { quantity = value; }
    public String getQuantityUnit() { return quantityUnit; }
    public void setQuantityUnit(String value) { quantityUnit = value; }
    public FoodType getFoodType() { return foodType; }
    public void setFoodType(FoodType value) { foodType = value; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime value) { expiryTime = value; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String value) { pickupAddress = value; }
    public String getCity() { return city; }
    public void setCity(String value) { city = value; }
    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate value) { pickupDate = value; }
    public LocalTime getPickupTime() { return pickupTime; }
    public void setPickupTime(LocalTime value) { pickupTime = value; }
    public String getAdditionalInstructions() { return additionalInstructions; }
    public void setAdditionalInstructions(String value) { additionalInstructions = value; }
    public DonationStatus getStatus() { return status; }
    public void setStatus(DonationStatus value) { status = value; }
    public User getDonor() { return donor; }
    public void setDonor(User value) { donor = value; }
    public User getVolunteer() { return volunteer; }
    public void setVolunteer(User value) { volunteer = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}