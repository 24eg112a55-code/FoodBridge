package com.communityfoodsharing.entity;

import com.communityfoodsharing.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 120) private String fullName;
    @Column(nullable = false, unique = true, length = 180) private String email;
    @Column(nullable = false, length = 32) private String phone;
    @Column(nullable = false) private String password;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Role role;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false) private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate void updateTimestamp() { updatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String value) { fullName = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getPassword() { return password; }
    public void setPassword(String value) { password = value; }
    public Role getRole() { return role; }
    public void setRole(Role value) { role = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}