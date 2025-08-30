package com.jiuyin.ui;

import com.jiuyin.config.AppConfig;
import com.jiuyin.function.task.taskmanager.TaskExecutor;
import com.jiuyin.util.LogUtils;
import com.jiuyin.nativeapi.CLibrary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 负责初始化所有 UI 组件、布局、事件监听器
 */
public class MainFrame extends JFrame {
    private TaskExecutor taskExecutor;
    private boolean isTaskRunning = false;

    private WindowSelectionPanel windowSelectionPanel;
    private SingleTaskPanel singleTaskPanel;
    private TaskQueuePanel taskQueuePanel;
    private LogPanel logPanel;
    private ButtonPanel buttonPanel;
    private HotkeyManager hotkeyManager;

    public MainFrame() {
        setTitle("九阴工具");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // 初始化组件
        initializeComponents();

        // 初始化任务执行器
        taskExecutor = new TaskExecutor(logPanel.getLogArea());

        // 检查设备连接状态
        checkDeviceConnection();

        // 注册监听器
        registerListeners();

        // 注册键盘热键
        hotkeyManager = new HotkeyManager(this);
        hotkeyManager.registerHotkeys();

        // 刷新窗口列表
        windowSelectionPanel.refreshWindowList();

        // 更新按钮状态
        updateButtonStates();

        // 窗口关闭时清理资源
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });

        setVisible(true);
    }

    private void initializeComponents() {
        // 顶部窗口选择区域
        windowSelectionPanel = new WindowSelectionPanel();
        add(windowSelectionPanel, BorderLayout.NORTH);

        // 中部区域 - 选项卡
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        singleTaskPanel = new SingleTaskPanel();
        tabs.addTab("单任务执行", singleTaskPanel);

        taskQueuePanel = new TaskQueuePanel();
        tabs.addTab("顺序任务执行", taskQueuePanel);

        add(tabs, BorderLayout.CENTER);

        // 右侧按钮面板
        buttonPanel = new ButtonPanel();
        add(buttonPanel, BorderLayout.EAST);

        // 底部日志面板
        logPanel = new LogPanel();
        add(logPanel, BorderLayout.SOUTH);
    }

    private void checkDeviceConnection() {
        if (CLibrary.INSTANCE.IsDeviceConnected() > 0) {
            LogUtils.writeLog(logPanel.getLogArea(), "设备连接成功");
        } else {
            LogUtils.writeLog(logPanel.getLogArea(), "设备未连接");
        }
    }

    private void registerListeners() {
        buttonPanel.getBtnStart().addActionListener(e -> handleStartAction());
        buttonPanel.getBtnStop().addActionListener(e -> handleStopAction());
        buttonPanel.getBtnSettings().addActionListener(e -> handleSettingsAction());

        taskQueuePanel.getBtnAddToQueue().addActionListener(e -> handleAddToQueue());
        taskQueuePanel.getBtnClearQueue().addActionListener(e -> handleClearQueue());
        taskQueuePanel.getBtnMoveUp().addActionListener(e -> handleMoveUp());
        taskQueuePanel.getBtnMoveDown().addActionListener(e -> handleMoveDown());
        taskQueuePanel.getBtnRemove().addActionListener(e -> handleRemove());
        taskQueuePanel.getBtnSaveSequence().addActionListener(e -> handleSaveSequence());
        taskQueuePanel.getBtnLoadSequence().addActionListener(e -> handleLoadSequence());
        taskQueuePanel.getBtnManageSequences().addActionListener(e -> handleManageSequences());

        windowSelectionPanel.getBtnRefreshWindows().addActionListener(e -> handleRefreshWindows());
        windowSelectionPanel.getBtnSelectWindow().addActionListener(e -> handleSelectWindow());
    }

    private void cleanup() {
        if (taskExecutor != null) {
            taskExecutor.shutdownThreadPool();
        }
        System.exit(0);
    }

    // Action handlers
    private void handleRefreshWindows() {
        windowSelectionPanel.refreshWindowList();
    }

    private void handleSelectWindow() {
        windowSelectionPanel.selectWindow(logPanel.getLogArea());
        updateButtonStates();
    }

    public void handleStartAction() {
        if (windowSelectionPanel.getSelectedWindowHandle() == null) {
            LogUtils.writeLog(logPanel.getLogArea(), "请先选择一个窗口");
            return;
        }

        if (!taskQueuePanel.isQueueEmpty()) {
            // 执行序列任务
            taskExecutor.executeTaskSequence(taskQueuePanel.getTaskSequence());
        } else {
            // 执行单任务
            String taskType = singleTaskPanel.getSelectedTask();
            if (taskType != null) {
                taskExecutor.executeSingleTask(taskType);
            }
        }

        isTaskRunning = true;
        updateButtonStates();
    }

    public void handleStopAction() {
        taskExecutor.cancelCurrentTask();
        isTaskRunning = false;
        updateButtonStates();
    }

    private void handleAddToQueue() {
        taskQueuePanel.addToQueue();
        updateButtonStates();
    }

    private void handleClearQueue() {
        taskQueuePanel.clearQueue();
        updateButtonStates();
    }

    private void handleMoveUp() {
        taskQueuePanel.moveUp();
    }

    private void handleMoveDown() {
        taskQueuePanel.moveDown();
    }

    private void handleRemove() {
        taskQueuePanel.removeSelected();
        updateButtonStates();
    }

    private void handleSaveSequence() {
        taskQueuePanel.saveSequence(logPanel.getLogArea());
        updateButtonStates();
    }

    private void handleLoadSequence() {
        taskQueuePanel.loadSequence(logPanel.getLogArea());
    }

    private void handleManageSequences() {
        SequenceManagerDialog managerDialog = new SequenceManagerDialog(this, logPanel.getLogArea());
        managerDialog.setVisible(true);
        taskQueuePanel.updateSavedSequencesCombo();
    }

    private void handleSettingsAction() {
        SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.setVisible(true);

        hotkeyManager.registerHotkeys();
        updateButtonStates();

        LogUtils.writeLog(logPanel.getLogArea(), "程序设置已更新");
    }

    public void updateButtonStates() {
        buttonPanel.updateButtonStates(isTaskRunning, windowSelectionPanel.getSelectedWindowHandle() != null);
        taskQueuePanel.updateButtonStates(isTaskRunning);

        // 更新开始/停止按钮文本
        if (isTaskRunning) {
            buttonPanel.getBtnStart().setText("开始执行(" + AppConfig.getStartHotkey() + ")");
            buttonPanel.getBtnStop().setText("停止执行(" + AppConfig.getStopHotkey() + ")");
        } else {
            buttonPanel.getBtnStart().setText("开始执行(" + AppConfig.getStartHotkey() + ")");
            buttonPanel.getBtnStop().setText("停止执行(" + AppConfig.getStopHotkey() + ")");
        }
    }

    public boolean isTaskRunning() {
        return isTaskRunning;
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public JTextArea getLogArea() {
        return logPanel.getLogArea();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new MainFrame();
    }
}