package com.capstone.scheduler.dsa;

import java.util.*;

public class TaskGraph {
    // Adjacency list: Key = parentTaskId, Value = list of childTaskIds that depend on the parent
    private final Map<Long, List<Long>> adjList = new HashMap<>();
    
    // Set of all unique task nodes in the graph
    private final Set<Long> nodes = new HashSet<>();

    /**
     * Adds a node (task) to the graph.
     */
    public void addNode(Long taskId) {
        nodes.add(taskId);
        adjList.putIfAbsent(taskId, new ArrayList<>());
    }

    /**
     * Adds a directed dependency: parentTaskId -> childTaskId (parentTaskId executes before childTaskId).
     */
    public void addDependency(Long parentTaskId, Long childTaskId) {
        addNode(parentTaskId);
        addNode(childTaskId);
        adjList.get(parentTaskId).add(childTaskId);
    }

    /**
     * Detects circular dependencies in the graph using DFS.
     * Uses 3-color code: 0 = unvisited, 1 = visiting (currently on recursion stack), 2 = fully visited.
     */
    public boolean hasCycle() {
        Map<Long, Integer> visited = new HashMap<>();
        for (Long node : nodes) {
            visited.put(node, 0); // Initialize as unvisited
        }

        for (Long node : nodes) {
            if (visited.get(node) == 0) {
                if (dfsHasCycle(node, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dfsHasCycle(Long u, Map<Long, Integer> visited) {
        visited.put(u, 1); // Mark as visiting

        List<Long> neighbors = adjList.getOrDefault(u, Collections.emptyList());
        for (Long v : neighbors) {
            Integer state = visited.get(v);
            if (state == null) continue;

            if (state == 1) {
                return true; // Cycle detected: visiting node encountered again
            } else if (state == 0) {
                if (dfsHasCycle(v, visited)) {
                    return true;
                }
            }
        }

        visited.put(u, 2); // Mark as fully visited
        return false;
    }

    /**
     * Performs Kahn's algorithm (topological sort) to compute the safe execution sequence.
     * Throws an exception or returns empty if a circular dependency is detected.
     */
    public List<Long> topologicalSort() {
        if (hasCycle()) {
            throw new IllegalStateException("Circular dependency detected! Topological sort is not possible.");
        }

        Map<Long, Integer> inDegree = new HashMap<>();
        // Initialize in-degrees for all nodes
        for (Long node : nodes) {
            inDegree.put(node, 0);
        }

        // Calculate in-degrees
        for (Map.Entry<Long, List<Long>> entry : adjList.entrySet()) {
            for (Long child : entry.getValue()) {
                inDegree.put(child, inDegree.getOrDefault(child, 0) + 1);
            }
        }

        // Queue for source nodes (in-degree 0)
        Queue<Long> queue = new LinkedList<>();
        for (Long node : nodes) {
            if (inDegree.get(node) == 0) {
                queue.add(node);
            }
        }

        List<Long> executionOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            Long u = queue.poll();
            executionOrder.add(u);

            List<Long> neighbors = adjList.getOrDefault(u, Collections.emptyList());
            for (Long v : neighbors) {
                inDegree.put(v, inDegree.get(v) - 1);
                if (inDegree.get(v) == 0) {
                    queue.add(v);
                }
            }
        }

        return executionOrder;
    }

    public Map<Long, List<Long>> getAdjList() {
        return adjList;
    }

    public Set<Long> getNodes() {
        return nodes;
    }
}
