package com.capstone.scheduler.controller;

import com.capstone.scheduler.algorithms.SortingAlgorithms;
import com.capstone.scheduler.dto.TaskDto;
import com.capstone.scheduler.service.TaskService;
import com.capstone.scheduler.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Map<String, Integer> PRIORITY_RANK = Map.of(
            "HIGH", 0, "MEDIUM", 1, "LOW", 2
    );

    private final TaskService taskService;
    private final SearchService searchService;

    public TaskController(TaskService taskService, SearchService searchService) {
        this.taskService = taskService;
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "sortBy", required = false) String sortBy) {
        List<TaskDto> tasks;
        if (query != null && !query.trim().isEmpty()) {
            tasks = new ArrayList<>(searchService.search(query));
        } else {
            tasks = new ArrayList<>(taskService.getTasks());
        }
        applySort(tasks, sortBy);
        return ResponseEntity.ok(tasks);
    }

    private void applySort(List<TaskDto> tasks, String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return;
        }
        switch (sortBy.trim().toLowerCase()) {
            case "priority" -> SortingAlgorithms.mergeSort(tasks, Comparator
                    .comparingInt((TaskDto t) -> PRIORITY_RANK.getOrDefault(
                            t.getPriority() != null ? t.getPriority().toUpperCase() : "MEDIUM", 1)));
            case "duedate", "due_date" -> SortingAlgorithms.quickSort(tasks, Comparator
                    .comparing(TaskDto::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
            case "title" -> SortingAlgorithms.mergeSort(tasks, Comparator
                    .comparing(t -> t.getTitle() != null ? t.getTitle().toLowerCase() : "",
                            Comparator.naturalOrder()));
            default -> { /* leave original order */ }
        }
    }

    @GetMapping("/archived")
    public ResponseEntity<List<TaskDto>> getArchivedTasks() {
        return ResponseEntity.ok(taskService.getArchivedTasks());
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskDto dto) {
        return ResponseEntity.ok(taskService.createTask(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @RequestBody TaskDto dto) {
        return ResponseEntity.ok(taskService.updateTask(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<TaskDto> archiveTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.archiveTask(id));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<TaskDto> restoreTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.restoreTask(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<TaskDto> completeTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.markCompleted(id));
    }

    @PostMapping("/undo")
    public ResponseEntity<MapResponse> undoAction() {
        boolean success = taskService.undoLastAction();
        return ResponseEntity.ok(new MapResponse(success, success ? "Undo action successful" : "No actions to undo"));
    }

    @PostMapping("/redo")
    public ResponseEntity<MapResponse> redoAction() {
        boolean success = taskService.redoLastAction();
        return ResponseEntity.ok(new MapResponse(success, success ? "Redo action successful" : "No actions to redo"));
    }

    @GetMapping("/topological")
    public ResponseEntity<List<TaskDto>> getTopologicalOrder() {
        return ResponseEntity.ok(taskService.getTopologicalExecutionOrder());
    }

    // Helper inner class for simple structured JSON message responses
    private static class MapResponse {
        private final boolean success;
        private final String message;

        public MapResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
