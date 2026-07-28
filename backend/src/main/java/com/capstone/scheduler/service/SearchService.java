package com.capstone.scheduler.service;

import com.capstone.scheduler.algorithms.KmpSearch;
import com.capstone.scheduler.dto.TaskDto;
import com.capstone.scheduler.engine.TrieSearchManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SearchService {

    private final TrieSearchManager trieSearchManager;
    private final TaskService taskService;

    public SearchService(TrieSearchManager trieSearchManager, TaskService taskService) {
        this.trieSearchManager = trieSearchManager;
        this.taskService = taskService;
    }

    /**
     * Searches tasks using a hybrid approach:
     * 1. Query the custom Trie index to match title, category name, or tags by prefix.
     * 2. Query the description using custom KMP String Matching as a fallback content scan.
     */
    @Transactional(readOnly = true)
    public List<TaskDto> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return taskService.getTasks();
        }

        // 1. Query the Trie index
        Set<Long> trieMatchedIds = trieSearchManager.search(query);

        // 2. Load active tasks for the current user and check matches
        List<TaskDto> userTasks = taskService.getTasks();
        List<TaskDto> matchedResults = new ArrayList<>();

        for (TaskDto task : userTasks) {
            boolean trieMatch = trieMatchedIds.contains(task.getId());
            
            // Check descriptions using KMP search
            boolean kmpMatch = KmpSearch.search(task.getDescription(), query);

            if (trieMatch || kmpMatch) {
                matchedResults.add(task);
            }
        }

        return matchedResults;
    }
}
