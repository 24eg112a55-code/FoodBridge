package com.communityfoodsharing.controller;

import com.communityfoodsharing.dto.Dtos.NotificationView;
import com.communityfoodsharing.security.CustomUserDetails;
import com.communityfoodsharing.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notifications;
    public NotificationController(NotificationService notifications) { this.notifications = notifications; }
    @GetMapping public List<NotificationView> list(@AuthenticationPrincipal CustomUserDetails user) { return notifications.list(user.getUser()); }
    @PutMapping("/{id}/read") public NotificationView markRead(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id) { return notifications.markRead(user.getUser(), id); }
}