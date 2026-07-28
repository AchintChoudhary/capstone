package com.capstone.scheduler.engine;

public interface TaskCommand {
    void execute();
    void undo();
    String getDescription();
}
