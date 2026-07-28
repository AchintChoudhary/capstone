package com.capstone.scheduler.engine;

import com.capstone.scheduler.dsa.Trie;
import com.capstone.scheduler.model.Task;
import com.capstone.scheduler.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TrieSearchManager {

    private static final Logger log = LoggerFactory.getLogger(TrieSearchManager.class);

    private final Trie trie = new Trie();
    private final TaskRepository taskRepository;

    public TrieSearchManager(@Lazy TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostConstruct
    public void buildInitialIndex() {
        try {
            log.info("Building initial Trie search index for tasks...");
            List<Task> tasks = taskRepository.findAll();
            for (Task task : tasks) {
                indexTask(task);
            }
            log.info("Trie search index initialized with {} tasks.", tasks.size());
        } catch (Exception e) {
            log.error("Failed to build initial search index", e);
        }
    }

    public void indexTask(Task task) {
        if (task == null || task.getId() == null) return;
        
        Long taskId = task.getId();
        
        if (task.getTitle() != null) {
            for (String token : tokenize(task.getTitle())) {
                trie.insert(token, taskId);
            }
        }

        if (task.getCategory() != null && task.getCategory().getName() != null) {
            for (String token : tokenize(task.getCategory().getName())) {
                trie.insert(token, taskId);
            }
        }

        if (task.getTags() != null) {
            for (String tag : task.getTags()) {
                for (String token : tokenize(tag)) {
                    trie.insert(token, taskId);
                }
            }
        }
    }

    public void deindexTask(Task task) {
        if (task == null || task.getId() == null) return;
        
        Long taskId = task.getId();

        if (task.getTitle() != null) {
            for (String token : tokenize(task.getTitle())) {
                trie.delete(token, taskId);
            }
        }

        if (task.getCategory() != null && task.getCategory().getName() != null) {
            for (String token : tokenize(task.getCategory().getName())) {
                trie.delete(token, taskId);
            }
        }

        if (task.getTags() != null) {
            for (String tag : task.getTags()) {
                for (String token : tokenize(tag)) {
                    trie.delete(token, taskId);
                }
            }
        }
    }

    public void reindexTask(Task oldTask, Task newTask) {
        deindexTask(oldTask);
        indexTask(newTask);
    }

    public Set<Long> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptySet();
        }

        String[] tokens = query.toLowerCase().trim().split("\\s+");
        if (tokens.length == 0) return Collections.emptySet();

        Set<Long> results = null;
        for (String token : tokens) {
            Set<Long> tokenResults = trie.search(token);
            if (results == null) {
                results = new HashSet<>(tokenResults);
            } else {
                results.retainAll(tokenResults);
            }
            
            if (results.isEmpty()) {
                break;
            }
        }
        
        return results != null ? results : Collections.emptySet();
    }

    private List<String> tokenize(String input) {
        if (input == null) return Collections.emptyList();
        String[] tokens = input.toLowerCase().split("[^a-zA-Z0-9]+");
        List<String> list = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isEmpty()) {
                list.add(token);
            }
        }
        return list;
    }
}
