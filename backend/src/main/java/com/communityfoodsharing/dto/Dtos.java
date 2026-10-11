package com.communityfoodsharing.dto;

import com.communityfoodsharing.enums.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public final class Dtos {
    private Dtos() {}
    public record RegisterRequest(@NotBlank @Size(max=120) String fullName, @NotBlank @Email @Size(max=180) String email,
        @NotBlank @Pattern(regexp="^[+0-9() -]{7,32}$") String phone, @NotBlank @Size(min=8,max=72) String password,
        @NotNull Role role) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, Role role, String name, String email) {}
    public record UserView(Long id, String fullName, String email, String phone, Role role, boolean active, LocalDateTime createdAt) {}
    public record ProfileRequest(@NotBlank @Size(max=120) String fullName, @NotBlank @Pattern(regexp="^[+0-9() -]{7,32}$") String phone) {}
    public record ProfileView(Long id, String fullName, String email, String phone, Role role, LocalDateTime createdAt) {}
    public record DonationRequest(@NotBlank @Size(max=120) String foodName, @NotBlank @Size(max=1200) String description,
        @NotNull @DecimalMin("0.01") BigDecimal quantity, @NotBlank @Size(max=32) String quantityUnit,
        @NotNull FoodType foodType, @NotNull LocalDateTime expiryTime, @NotBlank @Size(max=300) String pickupAddress,
        @NotBlank @Size(max=100) String city, @NotNull LocalDate pickupDate, @NotNull LocalTime pickupTime,
        @Size(max=1200) String additionalInstructions) {}
    public record DistributionRequest(@NotBlank @Size(max=300) String distributionLocation,
        @NotBlank @Size(max=160) String organizationName, @Min(1) @Max(100000) int peopleServed,
        @Size(max=1200) String distributionNotes) {}
    public record StatusRequest(@NotNull DonationStatus status) {}
    public record ActiveRequest(boolean active) {}
    public record DonationView(Long id, String foodName, String description, BigDecimal quantity, String quantityUnit,
        FoodType foodType, LocalDateTime expiryTime, String pickupAddress, String city, LocalDate pickupDate,
        LocalTime pickupTime, String additionalInstructions, DonationStatus status, Long donorId, String donorName,
        String donorPhone, Long volunteerId, String volunteerName, LocalDateTime createdAt,
        String distributionLocation, String organizationName, Integer peopleServed, String distributionNotes,
        LocalDateTime distributedAt, LocalDateTime completedAt) {}
    public record NotificationView(Long id, String message, String type, boolean read, LocalDateTime createdAt) {}
    public record DonorDashboard(long totalDonations, long availableDonations, long acceptedDonations, long completedDonations) {}
    public record VolunteerDashboard(long availableDonations, long myAcceptedDonations, long completedDeliveries, long peopleServed) {}
    public record AdminDashboard(long totalUsers, long totalDonors, long totalVolunteers, long totalDonations,
        long availableDonations, long acceptedDonations, long completedDonations, long peopleServed) {}
    public record MessageResponse(String message) {}
    public record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
}