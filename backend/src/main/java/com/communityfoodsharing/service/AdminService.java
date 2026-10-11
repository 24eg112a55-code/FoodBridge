package com.communityfoodsharing.service;

import com.communityfoodsharing.dto.Dtos.*;
import com.communityfoodsharing.entity.User;
import com.communityfoodsharing.enums.Role;
import com.communityfoodsharing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AdminService {
    private final UserRepository users;
    private final DonationRepository donations;
    private final DistributionRepository distributions;
    private final DonationService donationService;
    public AdminService(UserRepository users, DonationRepository donations, DistributionRepository distributions, DonationService donationService) {
        this.users = users; this.donations = donations; this.distributions = distributions; this.donationService = donationService;
    }
    @Transactional(readOnly = true)
    public AdminDashboard dashboard() {
        return new AdminDashboard(users.count(), users.countByRole(Role.DONOR), users.countByRole(Role.VOLUNTEER), donations.count(),
            donations.countByStatus(com.communityfoodsharing.enums.DonationStatus.AVAILABLE), donations.countByStatus(com.communityfoodsharing.enums.DonationStatus.ACCEPTED),
            donations.countByStatus(com.communityfoodsharing.enums.DonationStatus.COMPLETED), distributions.totalPeopleServed());
    }
    @Transactional(readOnly = true)
    public List<UserView> users(Role role, String query) {
        List<User> results = query == null || query.isBlank()
            ? (role == null ? users.findAll() : users.findByRole(role))
            : users.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query.trim(), query.trim());
        return results.stream().filter(user -> role == null || user.getRole() == role).map(this::userView).toList();
    }
    @Transactional
    public UserView setUserStatus(Long id, boolean active) {
        User user = users.findById(id).orElseThrow(() -> com.communityfoodsharing.exception.BusinessException.notFound("User not found"));
        user.setActive(active); return userView(user);
    }
    private UserView userView(User user) { return new UserView(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole(), user.isActive(), user.getCreatedAt()); }
}