package com.capstone.scheduler.dto;

import java.time.LocalDateTime;

public class ActivityLogDto {
    private Long id;
    private String eventType;
    private String message;
    private LocalDateTime createdAt;

    public ActivityLogDto() {}

    public ActivityLogDto(Long id, String eventType, String message, LocalDateTime createdAt) {
        this.id = id;
        this.eventType = eventType;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static ActivityLogDtoBuilder builder() {
        return new ActivityLogDtoBuilder();
    }

    public static class ActivityLogDtoBuilder {
        private Long id;
        private String eventType;
        private String message;
        private LocalDateTime createdAt;

        public ActivityLogDtoBuilder id(Long id) { this.id = id; return this; }
        public ActivityLogDtoBuilder eventType(String eventType) { this.eventType = eventType; return this; }
        public ActivityLogDtoBuilder message(String message) { this.message = message; return this; }
        public ActivityLogDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ActivityLogDto build() {
            return new ActivityLogDto(id, eventType, message, createdAt);
        }
    }
}
