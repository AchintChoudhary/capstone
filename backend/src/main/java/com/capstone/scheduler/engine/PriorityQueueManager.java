package com.capstone.scheduler.engine;

import com.capstone.scheduler.model.Task;
import com.capstone.scheduler.model.TaskStatus;
import com.capstone.scheduler.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class PriorityQueueManager {

    private static final Logger log = LoggerFactory.getLogger(PriorityQueueManager.class);

    private final TaskRepository taskRepository;
    private final WorkflowEngine workflowEngine;
    private final ExecutorService executorService;
    private final PriorityBlockingQueue<Task> executionQueue;
    private volatile boolean running = true;

    public PriorityQueueManager(TaskRepository taskRepository, @Lazy WorkflowEngine workflowEngine) {
        this.taskRepository = taskRepository;
        this.workflowEngine = workflowEngine;
        this.executionQueue = new PriorityBlockingQueue<>(50, 
            (t1, t2) -> Integer.compare(t2.getPriority().getLevel(), t1.getPriority().getLevel()));
        this.executorService = Executors.newFixedThreadPool(4); 
    }

    @PostConstruct
    public void startConsumer() {
        Thread consumerThread = new Thread(this::consumeQueue, "QueueConsumerThread");
        consumerThread.setDaemon(true);
        consumerThread.start();
        log.info("Priority Queue Manager worker threads initialized.");
    }

    public void submitTask(Task task) {
        if (task == null) return;
        log.info("Submitting task ID: {} to priority queue (Priority: {})", task.getId(), task.getPriority());
        executionQueue.offer(task);
    }

    private void consumeQueue() {
        while (running) {
            try {
                Task task = executionQueue.take();
                executorService.submit(() -> executeTask(task));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error retrieving task from execution queue", e);
            }
        }
    }

    private void executeTask(Task task) {
        try {
            log.info("Starting execution of task ID: {}, Title: {}", task.getId(), task.getTitle());
            
            Task dbTask = taskRepository.findById(task.getId()).orElse(null);
            if (dbTask == null || dbTask.getStatus() == TaskStatus.COMPLETED || dbTask.getStatus() == TaskStatus.ARCHIVED) {
                return;
            }

            dbTask.setStatus(TaskStatus.IN_PROGRESS);
            taskRepository.saveAndFlush(dbTask);

            TimeUnit.MILLISECONDS.sleep(2000);

            dbTask.setStatus(TaskStatus.COMPLETED);
            taskRepository.saveAndFlush(dbTask);
            log.info("Successfully executed task ID: {}", dbTask.getId());

            workflowEngine.onTaskCompleted(dbTask);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Task execution interrupted for ID: {}", task.getId(), e);
        } catch (Exception e) {
            log.error("Error executing task ID: {}", task.getId(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        log.info("Priority Queue Manager worker threads stopped.");
    }
}
