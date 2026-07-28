package com.capstone.scheduler.engine;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CommandStack {
    private final Map<Long, Stack<TaskCommand>> undoStacks = new ConcurrentHashMap<>();
    private final Map<Long, Stack<TaskCommand>> redoStacks = new ConcurrentHashMap<>();

    private Stack<TaskCommand> getUndoStack(Long userId) {
        return undoStacks.computeIfAbsent(userId, k -> new Stack<>());
    }

    private Stack<TaskCommand> getRedoStack(Long userId) {
        return redoStacks.computeIfAbsent(userId, k -> new Stack<>());
    }

    /**
     * Executes a task command, pushes it to the undo stack, and clears the redo stack.
     */
    public void executeCommand(Long userId, TaskCommand command) {
        command.execute();
        getUndoStack(userId).push(command);
        getRedoStack(userId).clear(); 
    }

    /**
     * Undoes the last command for this user.
     */
    public boolean undo(Long userId) {
        Stack<TaskCommand> undoStack = getUndoStack(userId);
        if (undoStack.isEmpty()) {
            return false;
        }
        TaskCommand command = undoStack.pop();
        command.undo();
        getRedoStack(userId).push(command);
        return true;
    }

    /**
     * Redoes the last undone command for this user.
     */
    public boolean redo(Long userId) {
        Stack<TaskCommand> redoStack = getRedoStack(userId);
        if (redoStack.isEmpty()) {
            return false;
        }
        TaskCommand command = redoStack.pop();
        command.execute();
        getUndoStack(userId).push(command);
        return true;
    }

    /**
     * Clears command history for a user.
     */
    public void clear(Long userId) {
        getUndoStack(userId).clear();
        getRedoStack(userId).clear();
    }
}
