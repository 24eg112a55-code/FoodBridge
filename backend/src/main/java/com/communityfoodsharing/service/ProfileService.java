package com.communityfoodsharing.service;

import com.communityfoodsharing.dto.Dtos.ProfileRequest;
import com.communityfoodsharing.dto.Dtos.ProfileView;
import com.communityfoodsharing.entity.User;
import com.communityfoodsharing.exception.BusinessException;
import com.communityfoodsharing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final UserRepository users;
    public ProfileService(UserRepository users) { this.users = users; }
    @Transactional(readOnly = true)
    public ProfileView get(User user) { return view(user); }
    @Transactional
    public ProfileView update(User user, ProfileRequest request) {
        User managed = users.findById(user.getId()).orElseThrow(() -> BusinessException.notFound("Account not found"));
        managed.setFullName(request.fullName().trim()); managed.setPhone(request.phone().trim());
        return view(managed);
    }
    private ProfileView view(User user) { return new ProfileView(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole(), user.getCreatedAt()); }
}