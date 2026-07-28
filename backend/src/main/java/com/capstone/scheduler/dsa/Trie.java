package com.capstone.scheduler.dsa;

import java.util.*;

public class Trie {
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        Set<Long> taskIds = new HashSet<>();
        boolean isEndOfWord = false;
    }

    private final TrieNode root = new TrieNode();

    /**
     * Inserts a word and maps it to a specific task ID.
     */
    public void insert(String word, Long taskId) {
        if (word == null || word.trim().isEmpty()) return;
        TrieNode current = root;
        String cleanWord = word.toLowerCase().trim();
        for (int i = 0; i < cleanWord.length(); i++) {
            char ch = cleanWord.charAt(i);
            current = current.children.computeIfAbsent(ch, k -> new TrieNode());
            current.taskIds.add(taskId);
        }
        current.isEndOfWord = true;
    }

    /**
     * Searches for task IDs matching a prefix.
     */
    public Set<Long> search(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) return Collections.emptySet();
        TrieNode current = root;
        String cleanPrefix = prefix.toLowerCase().trim();
        for (int i = 0; i < cleanPrefix.length(); i++) {
            char ch = cleanPrefix.charAt(i);
            current = current.children.get(ch);
            if (current == null) {
                return Collections.emptySet();
            }
        }
        return new HashSet<>(current.taskIds);
    }

    /**
     * Deletes a mapping of a task ID from a word.
     */
    public void delete(String word, Long taskId) {
        if (word == null || word.trim().isEmpty()) return;
        String cleanWord = word.toLowerCase().trim();
        deleteHelper(root, cleanWord, 0, taskId);
    }

    private boolean deleteHelper(TrieNode current, String word, int index, Long taskId) {
        if (current == null) return false;

        current.taskIds.remove(taskId);

        if (index == word.length()) {
            if (current.isEndOfWord) {
                current.isEndOfWord = false;
            }
            return current.children.isEmpty() && current.taskIds.isEmpty();
        }

        char ch = word.charAt(index);
        TrieNode nextNode = current.children.get(ch);
        if (nextNode == null) {
            return false;
        }

        boolean shouldDeleteChild = deleteHelper(nextNode, word, index + 1, taskId);
        if (shouldDeleteChild) {
            current.children.remove(ch);
            return current.children.isEmpty() && current.taskIds.isEmpty() && !current.isEndOfWord;
        }

        return false;
    }

    /**
     * Clear all records in the Trie.
     */
    public void clear() {
        root.children.clear();
        root.taskIds.clear();
    }
}
