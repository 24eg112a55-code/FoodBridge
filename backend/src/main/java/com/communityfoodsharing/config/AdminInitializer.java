package com.communityfoodsharing.config;

import com.communityfoodsharing.entity.User;
import com.communityfoodsharing.enums.Role;
import com.communityfoodsharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {
    @Bean CommandLineRunner initializeAdmin(UserRepository users, PasswordEncoder encoder,
        @Value("${app.admin.email:}") String email, @Value("${app.admin.password:}") String password,
        @Value("${app.admin.name:System Administrator}") String name, @Value("${app.admin.phone:}") String phone) {
        return args -> {
            if (email.isBlank() && password.isBlank()) return;
            if (email.isBlank() || password.length() < 12) throw new IllegalStateException("Set both ADMIN_EMAIL and an ADMIN_PASSWORD of at least 12 characters");
            if (users.existsByEmailIgnoreCase(email)) return;
            User admin = new User(); admin.setFullName(name); admin.setEmail(email.trim().toLowerCase());
            admin.setPhone(phone.isBlank() ? "0000000000" : phone); admin.setPassword(encoder.encode(password)); admin.setRole(Role.ADMIN);
            users.save(admin);
        };
    }
}