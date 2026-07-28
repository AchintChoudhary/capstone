package com.capstone.scheduler.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private String priority; 
    private String status;   
    private String recurrenceType; 
    private String cronExpression;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private Set<String> tags;
    private Set<Long> dependencyIds;

    public TaskDto() {}

    public TaskDto(Long id, String title, String description, LocalDateTime dueDate, String priority,
                   String status, String recurrenceType, String cronExpression, Long categoryId,
                   String categoryName, String categoryColor, Set<String> tags, Set<Long> dependencyIds) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.recurrenceType = recurrenceType;
        this.cronExpression = cronExpression;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryColor = categoryColor;
        this.tags = tags;
        this.dependencyIds = dependencyIds;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    public Set<Long> getDependencyIds() { return dependencyIds; }
    public void setDependencyIds(Set<Long> dependencyIds) { this.dependencyIds = dependencyIds; }

    public static TaskDtoBuilder builder() {
        return new TaskDtoBuilder();
    }

    public static class TaskDtoBuilder {
        private Long id;
        private String title;
        private String description;
        private LocalDateTime dueDate;
        private String priority;
        private String status;
        private String recurrenceType;
        private String cronExpression;
        private Long categoryId;
        private String categoryName;
        private String categoryColor;
        private Set<String> tags;
        private Set<Long> dependencyIds;

        public TaskDtoBuilder id(Long id) { this.id = id; return this; }
        public TaskDtoBuilder title(String title) { this.title = title; return this; }
        public TaskDtoBuilder description(String description) { this.description = description; return this; }
        public TaskDtoBuilder dueDate(LocalDateTime dueDate) { this.dueDate = dueDate; return this; }
        public TaskDtoBuilder priority(String priority) { this.priority = priority; return this; }
        public TaskDtoBuilder status(String status) { this.status = status; return this; }
        public TaskDtoBuilder recurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; return this; }
        public TaskDtoBuilder cronExpression(String cronExpression) { this.cronExpression = cronExpression; return this; }
        public TaskDtoBuilder categoryId(Long categoryId) { this.categoryId = categoryId; return this; }
        public TaskDtoBuilder categoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public TaskDtoBuilder categoryColor(String categoryColor) { this.categoryColor = categoryColor; return this; }
        public TaskDtoBuilder tags(Set<String> tags) { this.tags = tags; return this; }
        public TaskDtoBuilder dependencyIds(Set<Long> dependencyIds) { this.dependencyIds = dependencyIds; return this; }

        public TaskDto build() {
            return new TaskDto(id, title, description, dueDate, priority, status, recurrenceType,
                    cronExpression, categoryId, categoryName, categoryColor, tags, dependencyIds);
        }
    }
}
