package com.jiuyin.function.task;

/**
 * 任务接口
 */
public interface Task {
    String getName();
    void execute() throws InterruptedException;
    boolean isCompleted();
    void stop();
}