package com.capstone.scheduler.algorithms;

import com.capstone.scheduler.dsa.TaskGraph;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlgorithmTests {

    @Test
    public void testKmpSearch() {
        String text = "We are developing a Smart Task Scheduler project using Spring Boot.";
        
        // Positive tests
        assertTrue(KmpSearch.search(text, "smart"));
        assertTrue(KmpSearch.search(text, "spring boot"));
        assertTrue(KmpSearch.search(text, "smart task"));

        // Negative tests
        assertFalse(KmpSearch.search(text, "python"));
        assertFalse(KmpSearch.search(text, "database"));
    }

    @Test
    public void testTaskGraphTopologicalSort() {
        TaskGraph graph = new TaskGraph();

        // Build A -> B -> C graph
        graph.addDependency(1L, 2L); // 2 depends on 1 (1 runs before 2)
        graph.addDependency(2L, 3L); // 3 depends on 2 (2 runs before 3)

        assertFalse(graph.hasCycle());
        
        List<Long> order = graph.topologicalSort();
        assertEquals(3, order.size());
        assertEquals(1L, order.get(0));
        assertEquals(2L, order.get(1));
        assertEquals(3L, order.get(2));
    }

    @Test
    public void testTaskGraphCycleDetection() {
        TaskGraph graph = new TaskGraph();

        // Build cycle: 1 -> 2 -> 3 -> 1
        graph.addDependency(1L, 2L);
        graph.addDependency(2L, 3L);
        graph.addDependency(3L, 1L);

        assertTrue(graph.hasCycle());
        assertThrows(IllegalStateException.class, graph::topologicalSort);
    }

    @Test
    public void testSlidingWindowMovingAverage() {
        int[] data = {2, 4, 6, 8, 10};
        
        // Window size 3
        double[] average = SlidingWindow.calculateMovingAverage(data, 3);
        
        assertEquals(5, average.length);
        assertEquals(2.0, average[0]);                  // Average of [2]
        assertEquals(3.0, average[1]);                  // Average of [2, 4]
        assertEquals(4.0, average[2]);                  // Average of [2, 4, 6]
        assertEquals(6.0, average[3]);                  // Average of [4, 6, 8]
        assertEquals(8.0, average[4]);                  // Average of [6, 8, 10]
    }
}
