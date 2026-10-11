package com.communityfoodsharing.controller;

import com.communityfoodsharing.dto.Dtos.*;
import com.communityfoodsharing.security.CustomUserDetails;
import com.communityfoodsharing.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/donor")
public class DonorController {
    private final DonationService donations;
    public DonorController(DonationService donations) { this.donations = donations; }
    @GetMapping("/dashboard") public DonorDashboard dashboard(@AuthenticationPrincipal CustomUserDetails user) { return donations.donorDashboard(user.getUser()); }
    @PostMapping("/donations") @ResponseStatus(HttpStatus.CREATED)
    public DonationView create(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody DonationRequest request) { return donations.create(user.getUser(), request); }
    @GetMapping("/donations") public List<DonationView> list(@AuthenticationPrincipal CustomUserDetails user) { return donations.donorDonations(user.getUser()); }
    @GetMapping("/donations/{id}") public DonationView get(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id) { return donations.donorDonation(user.getUser(), id); }
    @PutMapping("/donations/{id}/cancel") public DonationView cancel(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id) { return donations.cancel(user.getUser(), id); }
}