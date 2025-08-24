package com.jiuyin.function.task;

import com.jiuyin.util.LogUtils;
import javax.swing.JTextArea;

/**
 * 抽象基础任务
 */
public abstract class BaseTask implements Task {
    protected final JTextArea logArea;
    protected volatile boolean running = false;
    protected volatile boolean completed = false;

    protected BaseTask(JTextArea logArea) {
        this.logArea = logArea;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public void stop() {
        running = false;
    }

    protected void log(String message) {
        LogUtils.writeLog(logArea, "[" + getName() + "] " + message);
    }
}