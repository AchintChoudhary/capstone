package com.capstone.scheduler.engine;

import com.capstone.scheduler.model.Task;
import com.capstone.scheduler.model.TaskStatus;
import com.capstone.scheduler.repository.TaskRepository;

public class CreateTaskCommand implements TaskCommand {
    private final TaskRepository taskRepository;
    private final TrieSearchManager trieSearchManager;
    private Task task;

    public CreateTaskCommand(TaskRepository taskRepository, TrieSearchManager trieSearchManager, Task task) {
        this.taskRepository = taskRepository;
        this.trieSearchManager = trieSearchManager;
        this.task = task;
    }

    @Override
    public void execute() {
        // If task was previously deleted/undone, it might need to be re-persisted.
        task.setStatus(TaskStatus.PENDING);
        task = taskRepository.saveAndFlush(task);
        trieSearchManager.indexTask(task);
    }

    @Override
    public void undo() {
        trieSearchManager.deindexTask(task);
        // Clean up from other tasks dependencies to prevent DB constraint issues
        taskRepository.findDependentTasks(task.getId()).forEach(dependent -> {
            dependent.getDependencies().remove(task);
            taskRepository.save(dependent);
        });
        taskRepository.delete(task);
    }

    @Override
    public String getDescription() {
        return "Create task '" + task.getTitle() + "'";
    }
}
