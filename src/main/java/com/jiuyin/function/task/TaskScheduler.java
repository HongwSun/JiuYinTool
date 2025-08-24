package com.jiuyin.function.task;

import com.jiuyin.util.LogUtils;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 任务调度器
 */
public class TaskScheduler {
    private final JTextArea logArea;
    private final ExecutorService taskThreadPool;
    private Future<?> currentScheduler;
    private final List<Task> taskQueue = new ArrayList<>();
    private volatile boolean running = false;

    public TaskScheduler(JTextArea logArea) {
        this.logArea = logArea;
        this.taskThreadPool = Executors.newSingleThreadExecutor(
                r -> new Thread(r, "Task-Scheduler-Thread")
        );
    }

    /**
     * 添加任务到队列
     */
    public void addTask(Task task) {
        taskQueue.add(task);
    }

    /**
     * 清空任务队列
     */
    public void clearTasks() {
        taskQueue.clear();
    }

    /**
     * 开始执行任务序列
     */
    public void start() {
        if (running) {
            LogUtils.writeLog(logArea, "调度器已经在运行");
            return;
        }

        if (taskQueue.isEmpty()) {
            LogUtils.writeLog(logArea, "任务队列为空");
            return;
        }

        running = true;
        currentScheduler = taskThreadPool.submit(() -> {
            try {
                for (int i = 0; i < taskQueue.size(); i++) {
                    if (!running) break;

                    Task task = taskQueue.get(i);
                    LogUtils.writeLog(logArea, "开始执行任务[" + (i + 1) + "/" + taskQueue.size() + "]: " + task.getName());

                    try {
                        task.execute();
                        if (task.isCompleted()) {
                            LogUtils.writeLog(logArea, "任务完成: " + task.getName());
                        } else {
                            LogUtils.writeLog(logArea, "任务中断: " + task.getName());
                            break;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LogUtils.writeLog(logArea, "任务被中断: " + task.getName());
                        break;
                    } catch (Exception e) {
                        LogUtils.writeLog(logArea, "任务执行异常: " + task.getName() + " - " + e.getMessage());
                        break;
                    }
                }
            } finally {
                running = false;
                LogUtils.writeLog(logArea, "任务调度完成");
            }
        });
    }

    /**
     * 停止所有任务
     */
    public void stop() {
        running = false;
        if (currentScheduler != null) {
            currentScheduler.cancel(true);
        }

        // 停止所有正在运行的任务
        for (Task task : taskQueue) {
            task.stop();
        }

        LogUtils.writeLog(logArea, "已停止所有任务");
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        stop();
        try {
            taskThreadPool.shutdown();
            if (!taskThreadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                taskThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            taskThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public boolean isRunning() {
        return running;
    }
}

