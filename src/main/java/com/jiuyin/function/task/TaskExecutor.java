package com.jiuyin.function.task;

import com.jiuyin.util.LogUtils;
import javax.swing.*;
import java.util.List;

/**
 * 任务执行器类，负责处理所有任务执行逻辑
 */
public class TaskExecutor {
    private final TaskScheduler taskScheduler;
    private final JTextArea logArea;

    public TaskExecutor(JTextArea logArea) {
        this.logArea = logArea;
        this.taskScheduler = new TaskScheduler(logArea);
    }

    /**
     * 执行单个任务
     */
    public void executeSingleTask(String taskType) {
        Task task = createTask(taskType);
        if (task != null) {
            taskScheduler.clearTasks();
            taskScheduler.addTask(task);
            LogUtils.writeLog(logArea, "准备执行单任务: " + taskType);
            new Thread(() -> taskScheduler.start()).start();
        }
    }

    /**
     * 执行任务序列
     */
    public void executeTaskSequence(List<String> taskTypes) {
        taskScheduler.clearTasks();
        LogUtils.writeLog(logArea, "准备执行任务序列，共 " + taskTypes.size() + " 个任务");

        for (int i = 0; i < taskTypes.size(); i++) {
            Task task = createTask(taskTypes.get(i));
            if (task != null) {
                taskScheduler.addTask(task);
                LogUtils.writeLog(logArea, "队列[" + (i + 1) + "]: " + taskTypes.get(i));
            }
        }
        new Thread(() -> taskScheduler.start()).start();
    }

    /**
     * 创建任务实例
     */
    private Task createTask(String taskType) {
        switch (taskType) {
            case "团练任务":
                return new TuanLianTask(logArea);

            case "种地任务":
                return new FarmTask(logArea);
            default:
                LogUtils.writeLog(logArea, "未知任务类型: " + taskType);
                return null;
        }
    }

    /**
     * 取消当前任务
     */
    public void cancelCurrentTask() {
        LogUtils.writeLog(logArea, "正在取消当前任务...");
        taskScheduler.stop();
    }

    /**
     * 关闭线程池
     */
    public void shutdownThreadPool() {
        taskScheduler.shutdown();
    }

    public boolean isTaskRunning() {
        return taskScheduler.isRunning();
    }
}

