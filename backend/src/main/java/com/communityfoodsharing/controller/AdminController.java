package com.communityfoodsharing.controller;

import com.communityfoodsharing.dto.Dtos.*;
import com.communityfoodsharing.enums.*;
import com.communityfoodsharing.service.AdminService;
import com.communityfoodsharing.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;
    private final DonationService donations;
    public AdminController(AdminService admin, DonationService donations) { this.admin = admin; this.donations = donations; }
    @GetMapping("/dashboard") public AdminDashboard dashboard() { return admin.dashboard(); }
    @GetMapping("/users") public List<UserView> users(@RequestParam(required=false) Role role, @RequestParam(required=false) String q) { return admin.users(role, q); }
    @PutMapping("/users/{id}/status") public UserView userStatus(@PathVariable Long id, @Valid @RequestBody ActiveRequest request) { return admin.setUserStatus(id, request.active()); }
    @GetMapping("/donations") public List<DonationView> donations(@RequestParam(required=false) DonationStatus status) { return this.donations.all(status); }
    @GetMapping("/donations/{id}") public DonationView donation(@PathVariable Long id) { return donations.adminDonation(id); }
}