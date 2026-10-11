package com.communityfoodsharing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = @Index(columnList = "user_id, is_read"))
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false, length = 300) private String message;
    @Column(nullable = false, length = 40) private String type;
    @Column(name = "is_read", nullable = false) private boolean isRead;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt = LocalDateTime.now();
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User value) { user = value; }
    public String getMessage() { return message; }
    public void setMessage(String value) { message = value; }
    public String getType() { return type; }
    public void setType(String value) { type = value; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean value) { isRead = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}