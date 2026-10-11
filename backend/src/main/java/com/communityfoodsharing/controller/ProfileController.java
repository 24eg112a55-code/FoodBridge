package com.communityfoodsharing.controller;

import com.communityfoodsharing.dto.Dtos.ProfileRequest;
import com.communityfoodsharing.dto.Dtos.ProfileView;
import com.communityfoodsharing.security.CustomUserDetails;
import com.communityfoodsharing.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profiles;
    public ProfileController(ProfileService profiles) { this.profiles = profiles; }
    @GetMapping public ProfileView get(@AuthenticationPrincipal CustomUserDetails user) { return profiles.get(user.getUser()); }
    @PutMapping public ProfileView update(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody ProfileRequest request) { return profiles.update(user.getUser(), request); }
}