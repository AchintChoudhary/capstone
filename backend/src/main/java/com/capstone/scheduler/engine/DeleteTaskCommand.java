package com.capstone.scheduler.engine;

import com.capstone.scheduler.model.Task;
import com.capstone.scheduler.repository.TaskRepository;
import java.util.ArrayList;
import java.util.List;

public class DeleteTaskCommand implements TaskCommand {
    private final TaskRepository taskRepository;
    private final TrieSearchManager trieSearchManager;
    private Task task;
    private List<Task> originalDependents;

    public DeleteTaskCommand(TaskRepository taskRepository, TrieSearchManager trieSearchManager, Task task) {
        this.taskRepository = taskRepository;
        this.trieSearchManager = trieSearchManager;
        this.task = task;
        this.originalDependents = new ArrayList<>();
    }

    @Override
    public void execute() {
        trieSearchManager.deindexTask(task);
        
        // Find tasks that depended on this task and save them to restore later
        originalDependents = taskRepository.findDependentTasks(task.getId());
        for (Task dependent : originalDependents) {
            dependent.getDependencies().remove(task);
            taskRepository.save(dependent);
        }

        taskRepository.delete(task);
    }

    @Override
    public void undo() {
        // Re-persist the task (clears the ID generation if necessary or preserves it)
        task.setId(null); // Save as new entity, which gets a new ID, but to preserve dependents we should restore relations
        task = taskRepository.saveAndFlush(task);
        trieSearchManager.indexTask(task);

        // Restore dependencies
        for (Task dependent : originalDependents) {
            Task dbDependent = taskRepository.findById(dependent.getId()).orElse(null);
            if (dbDependent != null) {
                dbDependent.getDependencies().add(task);
                taskRepository.save(dbDependent);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Delete task '" + task.getTitle() + "'";
    }
}
