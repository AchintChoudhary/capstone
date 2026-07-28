package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.ActivityLogDto;
import com.capstone.scheduler.service.ActivityLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public ResponseEntity<List<ActivityLogDto>> getLogs(
            @RequestParam(value = "limit", defaultValue = "50") int limit) {
        return ResponseEntity.ok(activityLogService.getRecentLogs(limit));
    }
}
