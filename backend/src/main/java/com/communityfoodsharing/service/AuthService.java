package com.communityfoodsharing.service;

import com.communityfoodsharing.dto.Dtos.*;
import com.communityfoodsharing.entity.User;
import com.communityfoodsharing.enums.Role;
import com.communityfoodsharing.exception.BusinessException;
import com.communityfoodsharing.repository.UserRepository;
import com.communityfoodsharing.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    public AuthService(UserRepository users, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users; this.encoder = encoder; this.authenticationManager = authenticationManager; this.jwtService = jwtService;
    }
    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) throw BusinessException.forbidden("Administrator accounts cannot be self-registered");
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) throw BusinessException.conflict("An account with this email already exists");
        User user = new User();
        user.setFullName(request.fullName().trim()); user.setEmail(email); user.setPhone(request.phone().trim());
        user.setPassword(encoder.encode(request.password())); user.setRole(request.role());
        users.save(user);
        return new MessageResponse("Registration successful. You can now sign in.");
    }
    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        User user = users.findByEmailIgnoreCase(principal.getUsername()).orElseThrow(() -> BusinessException.notFound("Account not found"));
        return new AuthResponse(jwtService.generateToken(principal), user.getRole(), user.getFullName(), user.getEmail());
    }
}