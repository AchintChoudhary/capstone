package com.capstone.scheduler.service;

import com.capstone.scheduler.dto.TaskDto;
import com.capstone.scheduler.engine.*;
import com.capstone.scheduler.model.*;
import com.capstone.scheduler.repository.CategoryRepository;
import com.capstone.scheduler.repository.TaskRepository;
import com.capstone.scheduler.repository.UserRepository;
import com.capstone.scheduler.dsa.TaskGraph;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final AuthService authService;
    private final TrieSearchManager trieSearchManager;
    private final CommandStack commandStack;
    private final WorkflowEngine workflowEngine;
    private final ActivityLogService activityLogService;

    public TaskService(TaskRepository taskRepository,
                       CategoryRepository categoryRepository,
                       AuthService authService,
                       TrieSearchManager trieSearchManager,
                       CommandStack commandStack,
                       @Lazy WorkflowEngine workflowEngine,
                       @Lazy ActivityLogService activityLogService) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.authService = authService;
        this.trieSearchManager = trieSearchManager;
        this.commandStack = commandStack;
        this.workflowEngine = workflowEngine;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getTasks() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return taskRepository.findActiveByUser(currentUser).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getArchivedTasks() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return taskRepository.findByUserAndStatus(currentUser, TaskStatus.ARCHIVED).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDto createTask(TaskDto dto) {
        User currentUser = authService.getCurrentAuthenticatedUser();

        // 1. Build initial entity
        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .priority(dto.getPriority() != null ? Priority.valueOf(dto.getPriority().toUpperCase()) : Priority.MEDIUM)
                .status(TaskStatus.PENDING)
                .recurrenceType(dto.getRecurrenceType() != null ? RecurrenceType.valueOf(dto.getRecurrenceType().toUpperCase()) : RecurrenceType.NONE)
                .cronExpression(dto.getCronExpression())
                .user(currentUser)
                .tags(dto.getTags() != null ? new HashSet<>(dto.getTags()) : new HashSet<>())
                .build();

        // 2. Set Category
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            task.setCategory(category);
        }

        // 3. Set dependencies and check circular dependencies
        if (dto.getDependencyIds() != null && !dto.getDependencyIds().isEmpty()) {
            checkCircularDependencies(task, dto.getDependencyIds());
            Set<Task> deps = new HashSet<>(taskRepository.findAllById(dto.getDependencyIds()));
            task.setDependencies(deps);
        }

        // 4. Wrap and execute via Command Pattern
        CreateTaskCommand command = new CreateTaskCommand(taskRepository, trieSearchManager, task);
        commandStack.executeCommand(currentUser.getId(), command);
        activityLogService.log("TASK_CREATE", "Created task: " + task.getTitle(), currentUser);

        return toDto(task);
    }

    @Transactional
    public TaskDto updateTask(Long id, TaskDto dto) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Task oldTask = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + id));

        if (!oldTask.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized action to edit this task.");
        }

        // Prepare new state task structure
        Task newTask = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .priority(dto.getPriority() != null ? Priority.valueOf(dto.getPriority().toUpperCase()) : Priority.MEDIUM)
                .status(dto.getStatus() != null ? TaskStatus.valueOf(dto.getStatus().toUpperCase()) : oldTask.getStatus())
                .recurrenceType(dto.getRecurrenceType() != null ? RecurrenceType.valueOf(dto.getRecurrenceType().toUpperCase()) : RecurrenceType.NONE)
                .cronExpression(dto.getCronExpression())
                .user(currentUser)
                .tags(dto.getTags() != null ? new HashSet<>(dto.getTags()) : new HashSet<>())
                .build();

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            newTask.setCategory(category);
        }

        if (dto.getDependencyIds() != null && !dto.getDependencyIds().isEmpty()) {
            checkCircularDependencies(oldTask, dto.getDependencyIds());
            Set<Task> deps = new HashSet<>(taskRepository.findAllById(dto.getDependencyIds()));
            newTask.setDependencies(deps);
        }

        // Execute via Command Pattern
        UpdateTaskCommand command = new UpdateTaskCommand(taskRepository, trieSearchManager, oldTask, newTask);
        commandStack.executeCommand(currentUser.getId(), command);

        // Reload from DB to return accurate representation
        Task savedTask = taskRepository.findById(id).orElseThrow();
        activityLogService.log("TASK_UPDATE", "Updated task: " + savedTask.getTitle(), currentUser);
        return toDto(savedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + id));

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized action to delete this task.");
        }

        DeleteTaskCommand command = new DeleteTaskCommand(taskRepository, trieSearchManager, task);
        commandStack.executeCommand(currentUser.getId(), command);
    }

    @Transactional
    public TaskDto archiveTask(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Task task = taskRepository.findById(id).orElseThrow();
        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized");
        }
        task.setStatus(TaskStatus.ARCHIVED);
        return toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDto restoreTask(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Task task = taskRepository.findById(id).orElseThrow();
        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized");
        }
        task.setStatus(TaskStatus.PENDING);
        return toDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDto markCompleted(Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        Task task = taskRepository.findById(id).orElseThrow();
        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Unauthorized");
        }

        task.setStatus(TaskStatus.COMPLETED);
        task = taskRepository.saveAndFlush(task);

        // Trigger workflow engine observers
        workflowEngine.onTaskCompleted(task);

        return toDto(task);
    }

    /**
     * Executes undo operation.
     */
    public boolean undoLastAction() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return commandStack.undo(currentUser.getId());
    }

    /**
     * Executes redo operation.
     */
    public boolean redoLastAction() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        return commandStack.redo(currentUser.getId());
    }

    /**
     * Generates a safe execution order of tasks using Topological Sort (Kahn's BFS Algorithm).
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getTopologicalExecutionOrder() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<Task> activeTasks = taskRepository.findActiveByUser(currentUser);

        TaskGraph graph = new TaskGraph();
        Map<Long, Task> lookup = new HashMap<>();

        // Add all nodes
        for (Task task : activeTasks) {
            graph.addNode(task.getId());
            lookup.put(task.getId(), task);
        }

        // Add all dependencies
        for (Task task : activeTasks) {
            for (Task parent : task.getDependencies()) {
                // Ensure parent task is part of active set (not archived/deleted)
                if (lookup.containsKey(parent.getId())) {
                    graph.addDependency(parent.getId(), task.getId());
                }
            }
        }

        try {
            List<Long> sortedIds = graph.topologicalSort();
            List<TaskDto> sortedTasks = new ArrayList<>();
            for (Long id : sortedIds) {
                Task t = lookup.get(id);
                if (t != null) {
                    sortedTasks.add(toDto(t));
                }
            }
            return sortedTasks;
        } catch (IllegalStateException e) {
            // Circular dependency found, fallback to default order and log it
            return activeTasks.stream().map(this::toDto).collect(Collectors.toList());
        }
    }

    private void checkCircularDependencies(Task targetTask, Set<Long> proposedDependencyIds) {
        TaskGraph graph = new TaskGraph();
        User user = targetTask.getUser();
        List<Task> allActive = taskRepository.findActiveByUser(user);

        for (Task t : allActive) {
            graph.addNode(t.getId());
        }

        Long targetId = targetTask.getId() != null ? targetTask.getId() : -1L;
        graph.addNode(targetId);

        for (Task t : allActive) {
            if (targetTask.getId() != null && t.getId().equals(targetTask.getId())) {
                continue; 
            }
            for (Task parent : t.getDependencies()) {
                if (graph.getNodes().contains(parent.getId())) {
                    graph.addDependency(parent.getId(), t.getId());
                }
            }
        }

        for (Long parentId : proposedDependencyIds) {
            if (parentId.equals(targetId)) {
                throw new IllegalArgumentException("A task cannot depend on itself.");
            }
            graph.addDependency(parentId, targetId);
        }

        if (graph.hasCycle()) {
            throw new IllegalStateException("Circular dependency detected in graph! This execution flow is blocked.");
        }
    }

    public TaskDto toDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .priority(task.getPriority().name())
                .status(task.getStatus().name())
                .recurrenceType(task.getRecurrenceType().name())
                .cronExpression(task.getCronExpression())
                .categoryId(task.getCategory() != null ? task.getCategory().getId() : null)
                .categoryName(task.getCategory() != null ? task.getCategory().getName() : null)
                .categoryColor(task.getCategory() != null ? task.getCategory().getColor() : null)
                .tags(new HashSet<>(task.getTags()))
                .dependencyIds(task.getDependencies().stream().map(Task::getId).collect(Collectors.toSet()))
                .build();
    }
}
