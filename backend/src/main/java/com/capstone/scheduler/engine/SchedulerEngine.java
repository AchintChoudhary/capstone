package com.capstone.scheduler.engine;

import com.capstone.scheduler.model.RecurrenceType;
import com.capstone.scheduler.model.Task;
import com.capstone.scheduler.model.TaskStatus;
import com.capstone.scheduler.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

@Component
public class SchedulerEngine {

    private static final Logger log = LoggerFactory.getLogger(SchedulerEngine.class);

    private final TaskRepository taskRepository;
    private final PriorityQueueManager priorityQueueManager;
    private final ScheduledExecutorService scheduledExecutorService;

    public SchedulerEngine(TaskRepository taskRepository, @Lazy PriorityQueueManager priorityQueueManager) {
        this.taskRepository = taskRepository;
        this.priorityQueueManager = priorityQueueManager;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @PostConstruct
    public void startScheduler() {
        scheduledExecutorService.scheduleAtFixedRate(this::pollDueTasks, 2, 5, TimeUnit.SECONDS);
        log.info("Scheduler Engine background polling thread initialized.");
    }

    private void pollDueTasks() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Task> dueTasks = taskRepository.findPendingTasksDueBefore(now);
            
            for (Task task : dueTasks) {
                boolean dependenciesSatisfied = true;
                for (Task parent : task.getDependencies()) {
                    if (parent.getStatus() != TaskStatus.COMPLETED) {
                        dependenciesSatisfied = false;
                        break;
                    }
                }

                if (dependenciesSatisfied) {
                    priorityQueueManager.submitTask(task);
                }
            }
        } catch (Exception e) {
            log.error("Error during task polling cycle in Scheduler Engine", e);
        }
    }

    public boolean handleRecurrence(Task completedTask) {
        if (completedTask.getRecurrenceType() == null || completedTask.getRecurrenceType() == RecurrenceType.NONE) {
            return false;
        }

        LocalDateTime currentDue = completedTask.getDueDate();
        if (currentDue == null) {
            currentDue = LocalDateTime.now();
        }

        LocalDateTime nextDue = calculateNextRun(currentDue, completedTask.getRecurrenceType(), completedTask.getCronExpression());
        
        completedTask.setDueDate(nextDue);
        completedTask.setStatus(TaskStatus.PENDING); 
        taskRepository.saveAndFlush(completedTask);

        log.info("Rescheduled recurring task ID: {} to run next at {}", completedTask.getId(), nextDue);
        return true;
    }

    private LocalDateTime calculateNextRun(LocalDateTime current, RecurrenceType type, String cronExpression) {
        switch (type) {
            case DAILY:
                return current.plusDays(1);
            case WEEKLY:
                return current.plusWeeks(1);
            case MONTHLY:
                return current.plusMonths(1);
            case CRON:
                return calculateCronNextRun(current, cronExpression);
            default:
                return current;
        }
    }

    private LocalDateTime calculateCronNextRun(LocalDateTime current, String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return current.plusMinutes(5);
        }

        try {
            String[] tokens = cronExpression.trim().split("\\s+");
            if (tokens.length >= 5) {
                String minutesToken = tokens[0];
                String hoursToken = tokens[1];

                if (minutesToken.startsWith("*/")) {
                    int interval = Integer.parseInt(minutesToken.substring(2));
                    return current.plusMinutes(interval);
                }
                
                if (minutesToken.equals("0") && hoursToken.equals("0")) {
                    return current.toLocalDate().plusDays(1).atStartOfDay();
                }
                
                if (minutesToken.equals("0") && hoursToken.equals("*")) {
                    return current.plusHours(1).withMinute(0).withSecond(0).withNano(0);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse custom cron expression: '{}'. Falling back to 5-minute schedule.", cronExpression);
        }

        return current.plusMinutes(5); 
    }

    @PreDestroy
    public void shutdown() {
        scheduledExecutorService.shutdown();
        try {
            if (!scheduledExecutorService.awaitTermination(3, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutorService.shutdownNow();
        }
        log.info("Scheduler Engine background polling thread stopped.");
    }
}
