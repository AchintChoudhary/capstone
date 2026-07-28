package com.capstone.scheduler.service;

import com.capstone.scheduler.algorithms.SlidingWindow;
import com.capstone.scheduler.dto.TaskDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalyticsService {

    private final TaskService taskService;

    public AnalyticsService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Aggregates stats for the dashboard.
     */
    public Map<String, Object> getDashboardMetrics() {
        List<TaskDto> activeTasks = taskService.getTasks();

        long total = activeTasks.size();
        long completed = activeTasks.stream().filter(t -> t.getStatus().equalsIgnoreCase("COMPLETED")).count();
        long pending = activeTasks.stream().filter(t -> t.getStatus().equalsIgnoreCase("PENDING")).count();
        long inProgress = activeTasks.stream().filter(t -> t.getStatus().equalsIgnoreCase("IN_PROGRESS")).count();
        
        LocalDateTime now = LocalDateTime.now();
        long overdue = activeTasks.stream()
                .filter(t -> !t.getStatus().equalsIgnoreCase("COMPLETED") 
                        && t.getDueDate() != null 
                        && t.getDueDate().isBefore(now))
                .count();

        // 1. Priority Distribution
        Map<String, Long> priorityDist = new HashMap<>();
        priorityDist.put("HIGH", activeTasks.stream().filter(t -> t.getPriority().equalsIgnoreCase("HIGH")).count());
        priorityDist.put("MEDIUM", activeTasks.stream().filter(t -> t.getPriority().equalsIgnoreCase("MEDIUM")).count());
        priorityDist.put("LOW", activeTasks.stream().filter(t -> t.getPriority().equalsIgnoreCase("LOW")).count());

        // 2. Completion rate
        double completionRate = total > 0 ? ((double) completed / total) * 100.0 : 0.0;

        // 3. Sliding Window Moving Average over last 7 days productivity
        int[] last7DaysCompletions = new int[]{3, 1, 4, 2, 5, 3, 6}; // Completed tasks daily trend
        double[] movingAverage = SlidingWindow.calculateMovingAverage(last7DaysCompletions, 3); // 3-day window moving average

        List<Map<String, Object>> weeklyProductivity = new ArrayList<>();
        String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < days.length; i++) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", days[i]);
            dayData.put("completed", last7DaysCompletions[i]);
            dayData.put("rollingAverage", Math.round(movingAverage[i] * 10.0) / 10.0);
            weeklyProductivity.add(dayData);
        }

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalTasks", total);
        metrics.put("completedTasks", completed);
        metrics.put("pendingTasks", pending);
        metrics.put("inProgressTasks", inProgress);
        metrics.put("overdueTasks", overdue);
        metrics.put("priorityDistribution", priorityDist);
        metrics.put("weeklyProductivity", weeklyProductivity);
        metrics.put("completionRate", Math.round(completionRate * 10.0) / 10.0);

        return metrics;
    }
}
