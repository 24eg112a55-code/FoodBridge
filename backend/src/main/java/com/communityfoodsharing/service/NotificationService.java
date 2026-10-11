package com.communityfoodsharing.service;

import com.communityfoodsharing.dto.Dtos.NotificationView;
import com.communityfoodsharing.entity.User;
import com.communityfoodsharing.exception.BusinessException;
import com.communityfoodsharing.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    public NotificationService(NotificationRepository notifications) { this.notifications = notifications; }
    @Transactional(readOnly = true)
    public List<NotificationView> list(User user) {
        return notifications.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .map(n -> new NotificationView(n.getId(), n.getMessage(), n.getType(), n.isRead(), n.getCreatedAt())).toList();
    }
    @Transactional
    public NotificationView markRead(User user, Long id) {
        var notification = notifications.findByIdAndUserId(id, user.getId()).orElseThrow(() -> BusinessException.notFound("Notification not found"));
        notification.setRead(true);
        return new NotificationView(notification.getId(), notification.getMessage(), notification.getType(), true, notification.getCreatedAt());
    }
}