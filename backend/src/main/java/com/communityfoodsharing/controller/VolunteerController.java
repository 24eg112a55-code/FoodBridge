package com.communityfoodsharing.controller;

import com.communityfoodsharing.dto.Dtos.*;
import com.communityfoodsharing.security.CustomUserDetails;
import com.communityfoodsharing.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {
    private final DonationService donations;
    public VolunteerController(DonationService donations) { this.donations = donations; }
    @GetMapping("/dashboard") public VolunteerDashboard dashboard(@AuthenticationPrincipal CustomUserDetails user) { return donations.volunteerDashboard(user.getUser()); }
    @GetMapping("/donations/available") public List<DonationView> available() { return donations.available(); }
    @GetMapping("/donations/my-pickups") public List<DonationView> pickups(@AuthenticationPrincipal CustomUserDetails user) { return donations.pickups(user.getUser(), false); }
    @GetMapping("/donations/completed") public List<DonationView> completed(@AuthenticationPrincipal CustomUserDetails user) { return donations.pickups(user.getUser(), true); }
    @PutMapping("/donations/{id}/accept") public DonationView accept(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id) { return donations.accept(user.getUser(), id); }
    @PutMapping("/donations/{id}/pickup") public DonationView pickup(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id) { return donations.pickup(user.getUser(), id); }
    @PutMapping("/donations/{id}/distribute")
    public DonationView distribute(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id, @Valid @RequestBody DistributionRequest request) { return donations.distribute(user.getUser(), id, request); }
    @PutMapping("/donations/{id}/complete") public DonationView complete(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id) { return donations.complete(user.getUser(), id); }
}