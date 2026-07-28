package com.capstone.scheduler.engine;

import com.capstone.scheduler.model.*;
import com.capstone.scheduler.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class WorkflowEngine {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

    private final WorkflowRepository workflowRepository;
    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PriorityQueueManager priorityQueueManager;
    private final SchedulerEngine schedulerEngine;

    public WorkflowEngine(WorkflowRepository workflowRepository,
                          TaskRepository taskRepository,
                          NotificationRepository notificationRepository,
                          ActivityLogRepository activityLogRepository,
                          PriorityQueueManager priorityQueueManager,
                          @Lazy SchedulerEngine schedulerEngine) {
        this.workflowRepository = workflowRepository;
        this.taskRepository = taskRepository;
        this.notificationRepository = notificationRepository;
        this.activityLogRepository = activityLogRepository;
        this.priorityQueueManager = priorityQueueManager;
        this.schedulerEngine = schedulerEngine;
    }

    /**
     * Triggered automatically whenever a task completes execution.
     */
    @Transactional
    public void onTaskCompleted(Task completedTask) {
        log.info("Workflow Engine received task completion event for ID: {}", completedTask.getId());

        // 1. Audit log and notification for task completion
        logTaskCompletion(completedTask);

        // 2. Process active workflows matching TASK_COMPLETED
        processWorkflows(completedTask);

        // 3. Process task dependencies
        processDependencies(completedTask);

        // 4. Reschedule if recurring
        if (completedTask.getRecurrenceType() != null && completedTask.getRecurrenceType() != com.capstone.scheduler.model.RecurrenceType.NONE) {
            schedulerEngine.handleRecurrence(completedTask);
        }
    }

    private void logTaskCompletion(Task completedTask) {
        ActivityLog logEntry = ActivityLog.builder()
                .eventType("TASK_UPDATE")
                .message("Task '" + completedTask.getTitle() + "' was successfully completed.")
                .user(completedTask.getUser())
                .build();
        activityLogRepository.save(logEntry);

        Notification notification = Notification.builder()
                .title("Task Completed")
                .message("Your task '" + completedTask.getTitle() + "' has been executed and completed.")
                .type("TASK_COMPLETED")
                .channel("IN_APP")
                .user(completedTask.getUser())
                .build();
        notificationRepository.save(notification);
    }

    private void processWorkflows(Task task) {
        List<Workflow> activeWorkflows = workflowRepository.findActiveByTriggerEvent("TASK_COMPLETED");
        
        for (Workflow workflow : activeWorkflows) {
            if (!workflow.getUser().getId().equals(task.getUser().getId())) {
                continue;
            }

            boolean conditionMet = evaluateCondition(workflow, task);
            if (conditionMet) {
                log.info("Condition met for workflow '{}'. Executing action: {}", workflow.getName(), workflow.getActionType());
                executeAction(workflow, task);
            }
        }
    }

    private boolean evaluateCondition(Workflow workflow, Task task) {
        String field = workflow.getConditionField();
        String val = workflow.getConditionValue();

        if (field == null || field.equalsIgnoreCase("NONE") || field.isEmpty()) {
            return true; 
        }

        if (field.equalsIgnoreCase("PRIORITY")) {
            return task.getPriority().name().equalsIgnoreCase(val);
        }

        if (field.equalsIgnoreCase("CATEGORY")) {
            return task.getCategory() != null && task.getCategory().getName().equalsIgnoreCase(val);
        }

        return false;
    }

    private void executeAction(Workflow workflow, Task task) {
        String actionType = workflow.getActionType();
        String actionVal = workflow.getActionValue();

        ActivityLog workflowLog = ActivityLog.builder()
                .eventType("WORKFLOW_EXEC")
                .message("Workflow '" + workflow.getName() + "' executed action: " + actionType + " on task: '" + task.getTitle() + "'")
                .user(task.getUser())
                .build();
        activityLogRepository.save(workflowLog);

        if (actionType.equalsIgnoreCase("SEND_EMAIL")) {
            String emailRecipient = (actionVal != null && !actionVal.isEmpty()) ? actionVal : task.getUser().getEmail();
            log.info("[MOCK EMAIL] Sending email to: {}. Subject: Workflow Completed. Body: Task '{}' met trigger conditions.",
                    emailRecipient, task.getTitle());
            
            Notification emailMockNotif = Notification.builder()
                    .title("Email Sent (Mock)")
                    .message("An email workflow alert has been triggered and sent to: " + emailRecipient + " for completed task '" + task.getTitle() + "'")
                    .type("WORKFLOW_COMPLETED")
                    .channel("EMAIL")
                    .user(task.getUser())
                    .build();
            notificationRepository.save(emailMockNotif);

        } else if (actionType.equalsIgnoreCase("CREATE_NOTIFICATION")) {
            Notification workflowNotif = Notification.builder()
                    .title("Workflow Automation Alert")
                    .message(actionVal != null ? actionVal : "Workflow '" + workflow.getName() + "' triggered for task '" + task.getTitle() + "'")
                    .type("WORKFLOW_COMPLETED")
                    .channel("IN_APP")
                    .user(task.getUser())
                    .build();
            notificationRepository.save(workflowNotif);

        } else if (actionType.equalsIgnoreCase("AUTO_ARCHIVE")) {
            task.setStatus(TaskStatus.ARCHIVED);
            taskRepository.save(task);
            log.info("Workflow auto-archived completed task ID: {}", task.getId());
            
            ActivityLog archiveLog = ActivityLog.builder()
                    .eventType("TASK_UPDATE")
                    .message("Task '" + task.getTitle() + "' was automatically archived by workflow '" + workflow.getName() + "'")
                    .user(task.getUser())
                    .build();
            activityLogRepository.save(archiveLog);
        }
    }

    private void processDependencies(Task completedTask) {
        List<Task> dependents = taskRepository.findDependentTasks(completedTask.getId());
        
        for (Task dependentTask : dependents) {
            boolean allCompleted = true;
            for (Task parent : dependentTask.getDependencies()) {
                if (parent.getStatus() != TaskStatus.COMPLETED) {
                    allCompleted = false;
                    break;
                }
            }
            
            if (allCompleted) {
                log.info("All dependencies resolved for task ID: {}. Submitting for priority execution.", dependentTask.getId());
                priorityQueueManager.submitTask(dependentTask);
            } else {
                log.info("Task ID: {} still waiting for other parent tasks to complete.", dependentTask.getId());
            }
        }
    }
}
