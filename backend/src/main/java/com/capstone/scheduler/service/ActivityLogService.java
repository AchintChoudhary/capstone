package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.ActivityLogDto;
import com.capstone.scheduler.model.ActivityLog;
import com.capstone.scheduler.model.User;
import com.capstone.scheduler.repository.ActivityLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final AuthService authService;

    public ActivityLogService(ActivityLogRepository activityLogRepository, AuthService authService) {
        this.activityLogRepository = activityLogRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDto> getRecentLogs(int limit) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        int pageSize = Math.min(Math.max(limit, 1), 200);
        return activityLogRepository.findUserAndSystemLogs(currentUser, PageRequest.of(0, pageSize))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void log(String eventType, String message, User user) {
        ActivityLog entry = ActivityLog.builder()
                .eventType(eventType)
                .message(message)
                .user(user)
                .build();
        activityLogRepository.save(entry);
    }

    private ActivityLogDto toDto(ActivityLog log) {
        return ActivityLogDto.builder()
                .id(log.getId())
                .eventType(log.getEventType())
                .message(log.getMessage())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
