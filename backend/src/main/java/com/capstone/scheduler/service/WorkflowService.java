package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.WorkflowDto;
import com.capstone.scheduler.model.User;
import com.capstone.scheduler.model.Workflow;
import com.capstone.scheduler.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final AuthService authService;

    public WorkflowService(WorkflowRepository workflowRepository, AuthService authService) {
        this.workflowRepository = workflowRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<WorkflowDto> getWorkflows() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return workflowRepository.findByUser(currentUser).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkflowDto createWorkflow(WorkflowDto dto) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        
        Workflow workflow = Workflow.builder()
                .name(dto.getName())
                .triggerEvent(dto.getTriggerEvent() != null ? dto.getTriggerEvent().toUpperCase() : "TASK_COMPLETED")
                .conditionField(dto.getConditionField() != null ? dto.getConditionField().toUpperCase() : "NONE")
                .conditionValue(dto.getConditionValue())
                .actionType(dto.getActionType() != null ? dto.getActionType().toUpperCase() : "CREATE_NOTIFICATION")
                .actionValue(dto.getActionValue())
                .isActive(true)
                .user(currentUser)
                .build();

        return toDto(workflowRepository.save(workflow));
    }

    @Transactional
    public WorkflowDto updateWorkflow(Long id, WorkflowDto dto) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));

        if (!workflow.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized");
        }

        workflow.setName(dto.getName());
        workflow.setTriggerEvent(dto.getTriggerEvent() != null ? dto.getTriggerEvent().toUpperCase() : workflow.getTriggerEvent());
        workflow.setConditionField(dto.getConditionField() != null ? dto.getConditionField().toUpperCase() : workflow.getConditionField());
        workflow.setConditionValue(dto.getConditionValue());
        workflow.setActionType(dto.getActionType() != null ? dto.getActionType().toUpperCase() : workflow.getActionType());
        workflow.setActionValue(dto.getActionValue());
        workflow.setActive(dto.isActive());

        return toDto(workflowRepository.save(workflow));
    }

    @Transactional
    public void deleteWorkflow(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found"));

        if (!workflow.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized");
        }

        workflowRepository.delete(workflow);
    }

    @Transactional
    public WorkflowDto toggleWorkflow(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Workflow workflow = workflowRepository.findById(id).orElseThrow();
        if (!workflow.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized");
        }
        workflow.setActive(!workflow.isActive());
        return toDto(workflowRepository.save(workflow));
    }

    private WorkflowDto toDto(Workflow w) {
        return WorkflowDto.builder()
                .id(w.getId())
                .name(w.getName())
                .triggerEvent(w.getTriggerEvent())
                .conditionField(w.getConditionField())
                .conditionValue(w.getConditionValue())
                .actionType(w.getActionType())
                .actionValue(w.getActionValue())
                .isActive(w.isActive())
                .build();
    }
}
