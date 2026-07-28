package com.capstone.scheduler.controller;

import com.capstone.scheduler.dto.WorkflowDto;
import com.capstone.scheduler.service.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping
    public ResponseEntity<List<WorkflowDto>> getWorkflows() {
        return ResponseEntity.ok(workflowService.getWorkflows());
    }

    @PostMapping
    public ResponseEntity<WorkflowDto> createWorkflow(@RequestBody WorkflowDto dto) {
        return ResponseEntity.ok(workflowService.createWorkflow(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkflowDto> updateWorkflow(@PathVariable Long id, @RequestBody WorkflowDto dto) {
        return ResponseEntity.ok(workflowService.updateWorkflow(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/toggle")
    public ResponseEntity<WorkflowDto> toggleWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.toggleWorkflow(id));
    }
}
