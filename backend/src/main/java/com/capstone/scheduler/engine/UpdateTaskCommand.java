package com.capstone.scheduler.engine;

import com.capstone.scheduler.model.Task;
import com.capstone.scheduler.repository.TaskRepository;
import java.util.HashSet;
import java.util.Set;

public class UpdateTaskCommand implements TaskCommand {
    private final TaskRepository taskRepository;
    private final TrieSearchManager trieSearchManager;
    private final Long taskId;
    private final TaskSnapshot oldState;
    private final TaskSnapshot newState;

    public UpdateTaskCommand(TaskRepository taskRepository, TrieSearchManager trieSearchManager, Task oldTask, Task newTask) {
        this.taskRepository = taskRepository;
        this.trieSearchManager = trieSearchManager;
        this.taskId = oldTask.getId();
        this.oldState = new TaskSnapshot(oldTask);
        this.newState = new TaskSnapshot(newTask);
    }

    @Override
    public void execute() {
        Task task = taskRepository.findById(taskId).orElseThrow();
        trieSearchManager.deindexTask(task);
        newState.restore(task);
        taskRepository.saveAndFlush(task);
        trieSearchManager.indexTask(task);
    }

    @Override
    public void undo() {
        Task task = taskRepository.findById(taskId).orElseThrow();
        trieSearchManager.deindexTask(task);
        oldState.restore(task);
        taskRepository.saveAndFlush(task);
        trieSearchManager.indexTask(task);
    }

    @Override
    public String getDescription() {
        return "Update task '" + oldState.title + "'";
    }

    private static class TaskSnapshot {
        String title;
        String description;
        java.time.LocalDateTime dueDate;
        com.capstone.scheduler.model.Priority priority;
        com.capstone.scheduler.model.TaskStatus status;
        com.capstone.scheduler.model.RecurrenceType recurrenceType;
        String cronExpression;
        com.capstone.scheduler.model.Category category;
        Set<String> tags;

        TaskSnapshot(Task task) {
            this.title = task.getTitle();
            this.description = task.getDescription();
            this.dueDate = task.getDueDate();
            this.priority = task.getPriority();
            this.status = task.getStatus();
            this.recurrenceType = task.getRecurrenceType();
            this.cronExpression = task.getCronExpression();
            this.category = task.getCategory();
            this.tags = new HashSet<>(task.getTags());
        }

        void restore(Task task) {
            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(dueDate);
            task.setPriority(priority);
            task.setStatus(status);
            task.setRecurrenceType(recurrenceType);
            task.setCronExpression(cronExpression);
            task.setCategory(category);
            task.setTags(new HashSet<>(tags));
        }
    }
}
