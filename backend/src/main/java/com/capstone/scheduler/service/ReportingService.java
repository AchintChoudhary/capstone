package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.TaskDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ReportingService {

    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    public ReportingService(TaskService taskService, ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.objectMapper = objectMapper;
    }

    /**
     * Exports task listings to CSV formatted byte array.
     */
    public byte[] exportTasksToCsv() {
        List<TaskDto> tasks = taskService.getTasks();
        StringBuilder sb = new StringBuilder();
        
        // CSV Header
        sb.append("ID,Title,Description,Due Date,Priority,Status,Recurrence Type,Category,Tags\n");

        for (TaskDto task : tasks) {
            sb.append(task.getId()).append(",")
              .append(escapeCsvField(task.getTitle())).append(",")
              .append(escapeCsvField(task.getDescription())).append(",")
              .append(task.getDueDate() != null ? task.getDueDate().toString() : "N/A").append(",")
              .append(task.getPriority()).append(",")
              .append(task.getStatus()).append(",")
              .append(task.getRecurrenceType()).append(",")
              .append(escapeCsvField(task.getCategoryName())).append(",")
              .append(escapeCsvField(String.join("; ", task.getTags() != null ? task.getTags() : List.of())))
              .append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Exports task listings as JSON.
     */
    public byte[] exportTasksToJson() {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(taskService.getTasks());
        } catch (JsonProcessingException e) {
            return "[]".getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Exports task summary to a formatted text report simulating PDF.
     */
    public byte[] exportTasksToTextReport() {
        List<TaskDto> tasks = taskService.getTasks();
        StringBuilder sb = new StringBuilder();

        sb.append("=========================================================================\n");
        sb.append("              SMART SCHEDULER & WORKFLOW SYSTEM REPORT                   \n");
        sb.append("=========================================================================\n");
        sb.append("Generated On: ").append(java.time.LocalDateTime.now()).append("\n");
        sb.append("Total Active Tasks: ").append(tasks.size()).append("\n");
        sb.append("-------------------------------------------------------------------------\n\n");

        for (TaskDto task : tasks) {
            sb.append(String.format("Task ID: %-5d | Title: %s\n", task.getId(), task.getTitle()));
            sb.append(String.format("Status:  %-10s | Priority: %-10s | Category: %s\n", 
                    task.getStatus(), task.getPriority(), task.getCategoryName() != null ? task.getCategoryName() : "None"));
            sb.append(String.format("Due Date: %-25s | Recurrence: %s\n", 
                    task.getDueDate() != null ? task.getDueDate().toString() : "N/A", task.getRecurrenceType()));
            sb.append(String.format("Description: %s\n", task.getDescription() != null ? task.getDescription() : "N/A"));
            sb.append(String.format("Tags: %s\n", task.getTags() != null ? String.join(", ", task.getTags()) : "None"));
            sb.append("-------------------------------------------------------------------------\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
