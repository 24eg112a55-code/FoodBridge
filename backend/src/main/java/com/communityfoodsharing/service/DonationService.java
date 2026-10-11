package com.communityfoodsharing.service;

import com.communityfoodsharing.dto.Dtos.*;
import com.communityfoodsharing.entity.*;
import com.communityfoodsharing.enums.DonationStatus;
import com.communityfoodsharing.exception.BusinessException;
import com.communityfoodsharing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DonationService {
    private final DonationRepository donations;
    private final DistributionRepository distributions;
    private final NotificationRepository notifications;
    public DonationService(DonationRepository donations, DistributionRepository distributions, NotificationRepository notifications) {
        this.donations = donations; this.distributions = distributions; this.notifications = notifications;
    }
    @Transactional
    public DonationView create(User donor, DonationRequest request) {
        if (!request.expiryTime().isAfter(LocalDateTime.now())) throw BusinessException.badRequest("Expiry time must be in the future");
        if (request.pickupDate().isBefore(java.time.LocalDate.now())) throw BusinessException.badRequest("Pickup date cannot be in the past");
        FoodDonation donation = new FoodDonation();
        donation.setFoodName(request.foodName().trim()); donation.setDescription(request.description().trim());
        donation.setQuantity(request.quantity()); donation.setQuantityUnit(request.quantityUnit().trim()); donation.setFoodType(request.foodType());
        donation.setExpiryTime(request.expiryTime()); donation.setPickupAddress(request.pickupAddress().trim()); donation.setCity(request.city().trim());
        donation.setPickupDate(request.pickupDate()); donation.setPickupTime(request.pickupTime());
        donation.setAdditionalInstructions(request.additionalInstructions()); donation.setDonor(donor); donation.setStatus(DonationStatus.AVAILABLE);
        DonationView view = view(donations.save(donation));
        notifyUser(donor, "Your donation " + donation.getFoodName() + " is now available for pickup.", "DONATION_AVAILABLE");
        return view;
    }
    @Transactional(readOnly = true)
    public List<DonationView> donorDonations(User donor) { return donations.findByDonorIdOrderByCreatedAtDesc(donor.getId()).stream().map(this::view).toList(); }
    @Transactional(readOnly = true)
    public DonationView donorDonation(User donor, Long id) {
        return view(donations.findByIdAndDonorId(id, donor.getId()).orElseThrow(() -> BusinessException.notFound("Donation not found")));
    }
    @Transactional
    public DonationView cancel(User donor, Long id) {
        FoodDonation donation = donations.findLockedById(id).orElseThrow(() -> BusinessException.notFound("Donation not found"));
        if (!donation.getDonor().getId().equals(donor.getId())) throw BusinessException.notFound("Donation not found");
        requireStatus(donation, DonationStatus.AVAILABLE);
        donation.setStatus(DonationStatus.CANCELLED);
        notifyUser(donor, "Donation " + donation.getFoodName() + " was cancelled.", "DONATION_CANCELLED");
        return view(donation);
    }
    @Transactional(readOnly = true)
    public List<DonationView> available() { return donations.findByStatusOrderByCreatedAtDesc(DonationStatus.AVAILABLE).stream().map(d -> view(d, false)).toList(); }
    @Transactional(readOnly = true)
    public List<DonationView> pickups(User volunteer, boolean completed) {
        return donations.findByVolunteerIdOrderByUpdatedAtDesc(volunteer.getId()).stream()
            .filter(d -> completed ? d.getStatus() == DonationStatus.COMPLETED : d.getStatus() != DonationStatus.COMPLETED && d.getStatus() != DonationStatus.CANCELLED)
            .map(this::view).toList();
    }
    @Transactional
    public DonationView accept(User volunteer, Long id) {
        FoodDonation donation = donations.findLockedById(id).orElseThrow(() -> BusinessException.notFound("Donation not found"));
        requireStatus(donation, DonationStatus.AVAILABLE);
        donation.setVolunteer(volunteer);
        donation.setStatus(DonationStatus.ACCEPTED);
        notifyUser(donation.getDonor(), volunteer.getFullName() + " accepted your donation " + donation.getFoodName() + ".", "DONATION_ACCEPTED");
        return view(donation);
    }
    @Transactional
    public DonationView pickup(User volunteer, Long id) {
        FoodDonation donation = assignedDonation(volunteer, id);
        requireStatus(donation, DonationStatus.ACCEPTED);
        donation.setStatus(DonationStatus.PICKED_UP);
        notifyUser(donation.getDonor(), "Your donation " + donation.getFoodName() + " has been picked up.", "FOOD_PICKED_UP");
        return view(donation);
    }
    @Transactional
    public DonationView distribute(User volunteer, Long id, DistributionRequest request) {
        FoodDonation donation = assignedDonation(volunteer, id);
        requireStatus(donation, DonationStatus.PICKED_UP);
        Distribution distribution = new Distribution();
        distribution.setDonation(donation); distribution.setDistributionLocation(request.distributionLocation().trim());
        distribution.setOrganizationName(request.organizationName().trim()); distribution.setPeopleServed(request.peopleServed());
        distribution.setDistributionNotes(request.distributionNotes()); distribution.setDistributedAt(LocalDateTime.now());
        distributions.save(distribution); donation.setStatus(DonationStatus.DISTRIBUTED);
        notifyUser(donation.getDonor(), "Your donation " + donation.getFoodName() + " has been distributed.", "FOOD_DISTRIBUTED");
        return view(donation);
    }
    @Transactional
    public DonationView complete(User volunteer, Long id) {
        FoodDonation donation = assignedDonation(volunteer, id);
        requireStatus(donation, DonationStatus.DISTRIBUTED);
        Distribution distribution = distributions.findByDonationId(id).orElseThrow(() -> BusinessException.conflict("Distribution details are required before completion"));
        distribution.setCompletedAt(LocalDateTime.now()); donation.setStatus(DonationStatus.COMPLETED);
        notifyUser(donation.getDonor(), "Donation " + donation.getFoodName() + " has been completed. Thank you for sharing.", "DONATION_COMPLETED");
        return view(donation);
    }
    @Transactional(readOnly = true)
    public DonorDashboard donorDashboard(User donor) {
        return new DonorDashboard(donations.countByDonorId(donor.getId()), donations.countByDonorIdAndStatus(donor.getId(), DonationStatus.AVAILABLE),
            donations.countByDonorIdAndStatus(donor.getId(), DonationStatus.ACCEPTED), donations.countByDonorIdAndStatus(donor.getId(), DonationStatus.COMPLETED));
    }
    @Transactional(readOnly = true)
    public VolunteerDashboard volunteerDashboard(User volunteer) {
        return new VolunteerDashboard(donations.countByStatus(DonationStatus.AVAILABLE), donations.countByVolunteerIdAndStatus(volunteer.getId(), DonationStatus.ACCEPTED),
            donations.countByVolunteerIdAndStatus(volunteer.getId(), DonationStatus.COMPLETED), distributions.peopleServedByVolunteer(volunteer.getId()));
    }
    @Transactional(readOnly = true)
    public List<DonationView> all(DonationStatus status) {
        return (status == null ? donations.findAll() : donations.findByStatusOrderByCreatedAtDesc(status)).stream().map(this::view).toList();
    }
    @Transactional(readOnly = true)
    public DonationView adminDonation(Long id) { return view(donations.findById(id).orElseThrow(() -> BusinessException.notFound("Donation not found"))); }
    private FoodDonation assignedDonation(User volunteer, Long id) {
        FoodDonation donation = donations.findLockedById(id).orElseThrow(() -> BusinessException.notFound("Donation not found"));
        if (donation.getVolunteer() == null || !donation.getVolunteer().getId().equals(volunteer.getId())) throw BusinessException.forbidden("This donation is assigned to another volunteer");
        return donation;
    }
    private void requireStatus(FoodDonation donation, DonationStatus expected) {
        if (donation.getStatus() != expected) throw BusinessException.conflict("Invalid status transition: donation is " + donation.getStatus() + ", expected " + expected);
    }
    private void notifyUser(User user, String message, String type) {
        Notification notification = new Notification(); notification.setUser(user); notification.setMessage(message); notification.setType(type); notifications.save(notification);
    }
    private DonationView view(FoodDonation donation) { return view(donation, true); }
    private DonationView view(FoodDonation donation, boolean includeDonorPhone) {
        Distribution distribution = distributions.findByDonationId(donation.getId()).orElse(null);
        User donor = donation.getDonor(); User volunteer = donation.getVolunteer();
        return new DonationView(donation.getId(), donation.getFoodName(), donation.getDescription(), donation.getQuantity(), donation.getQuantityUnit(),
            donation.getFoodType(), donation.getExpiryTime(), donation.getPickupAddress(), donation.getCity(), donation.getPickupDate(), donation.getPickupTime(),
            donation.getAdditionalInstructions(), donation.getStatus(), donor.getId(), donor.getFullName(), includeDonorPhone ? donor.getPhone() : null, volunteer == null ? null : volunteer.getId(),
            volunteer == null ? null : volunteer.getFullName(), donation.getCreatedAt(), distribution == null ? null : distribution.getDistributionLocation(),
            distribution == null ? null : distribution.getOrganizationName(), distribution == null ? null : distribution.getPeopleServed(),
            distribution == null ? null : distribution.getDistributionNotes(), distribution == null ? null : distribution.getDistributedAt(),
            distribution == null ? null : distribution.getCompletedAt());
    }
}