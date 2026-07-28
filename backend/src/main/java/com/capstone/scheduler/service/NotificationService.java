package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.NotificationDto;
import com.capstone.scheduler.model.Notification;
import com.capstone.scheduler.model.User;
import com.capstone.scheduler.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    public NotificationService(NotificationRepository notificationRepository, AuthService authService) {
        this.notificationRepository = notificationRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(currentUser).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .channel(n.getChannel())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
