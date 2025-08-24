package com.jiuyin.ui;

import com.jiuyin.config.AppConfig;
import com.jiuyin.function.task.TaskExecutor;
import com.jiuyin.nativeapi.CLibrary;
import com.jiuyin.util.LogUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class MainJpanel extends JFrame implements ActionListener {

    private final JButton btnSettings = new JButton("程序设置");
    private final JButton btnStart = new JButton("开始执行");
    private final JButton btnStop = new JButton("停止执行");
    private final JButton btnAddToQueue = new JButton("添加到队列");
    private final JButton btnClearQueue = new JButton("清空队列");
    private final JButton btnMoveUp = new JButton("上移");
    private final JButton btnMoveDown = new JButton("下移");
    private final JButton btnRemove = new JButton("移除");
    private final JButton btnSaveSequence = new JButton("保存序列");
    private final JButton btnLoadSequence = new JButton("加载序列");
    private final JButton btnManageSequences = new JButton("管理序列");

    private JTextArea logArea;
    private JList<String> taskQueueList;
    private DefaultListModel<String> queueListModel;
    private JComboBox<String> singleTaskComboBox;
    private JComboBox<String> sequentialTaskComboBox;
    private JComboBox<String> savedSequencesCombo;

    private TaskExecutor taskExecutor;
    private boolean isTaskRunning = false;

    public MainJpanel() {
        setTitle("九阴工具");
        setSize(800, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        /* ====== 中部区域 ====== */
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.addTab("单任务执行", buildSingleTaskPanel());
        tabs.addTab("顺序任务执行", buildSequentialTaskPanel());
        add(tabs, BorderLayout.CENTER);

        /* ====== 右侧按钮 ====== */
        JPanel btnPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton button = new JButton("目前只实现了团练按键");
        button.setEnabled(false);
        btnPanel.add(button);

        btnPanel.add(btnSettings);
        btnPanel.add(btnStart);
        btnPanel.add(btnStop);
        btnStop.setEnabled(false);

        add(btnPanel, BorderLayout.EAST);

        /* ====== 底部日志 ====== */
        logArea = new JTextArea(8, 0);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("日志"));
        add(logScroll, BorderLayout.SOUTH);

        // 初始化任务执行器
        taskExecutor = new TaskExecutor(logArea);

        // 检查设备连接状态
        if (CLibrary.INSTANCE.IsDeviceConnected() > 0) {
            LogUtils.writeLog(logArea, "设备连接成功");
        } else {
            LogUtils.writeLog(logArea, "设备未连接");
        }

        // 注册监听器
        btnStart.addActionListener(this);
        btnStop.addActionListener(this);
        btnSettings.addActionListener(this);
        btnAddToQueue.addActionListener(this);
        btnClearQueue.addActionListener(this);
        btnMoveUp.addActionListener(this);
        btnMoveDown.addActionListener(this);
        btnRemove.addActionListener(this);
        btnSaveSequence.addActionListener(this);
        btnLoadSequence.addActionListener(this);
        btnManageSequences.addActionListener(this);

        // 注册键盘热键
        registerHotkeys();

        // 更新按钮状态
        updateButtonStates();

        // 窗口关闭时清理资源
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (taskExecutor != null) {
                    taskExecutor.shutdownThreadPool();
                }
                System.exit(0);
            }
        });

        setVisible(true);
    }

    private JPanel buildSingleTaskPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 任务选择
        JPanel taskSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        taskSelectPanel.add(new JLabel("选择任务:"));

        singleTaskComboBox = new JComboBox<>();
        singleTaskComboBox.addItem("团练任务");
        singleTaskComboBox.addItem("种地任务");



        taskSelectPanel.add(singleTaskComboBox);

        panel.add(taskSelectPanel, BorderLayout.NORTH);

        // 说明文本
        JTextArea description = new JTextArea("在此模式下每次只执行一个选定的任务。\n适合单独完成某个特定任务。");
        description.setEditable(false);
        description.setBackground(panel.getBackground());
        description.setFont(new Font("宋体", Font.PLAIN, 12));
        panel.add(description, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildSequentialTaskPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 上部：任务选择和添加
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        JPanel taskSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        taskSelectPanel.add(new JLabel("选择任务:"));

        sequentialTaskComboBox = new JComboBox<>();
        sequentialTaskComboBox.addItem("团练任务");
        taskSelectPanel.add(sequentialTaskComboBox);
        taskSelectPanel.add(btnAddToQueue);

        topPanel.add(taskSelectPanel, BorderLayout.NORTH);

        // 序列管理面板
        JPanel sequenceManagementPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sequenceManagementPanel.add(new JLabel("保存的序列:"));

        savedSequencesCombo = new JComboBox<>();
        updateSavedSequencesCombo();
        sequenceManagementPanel.add(savedSequencesCombo);
        sequenceManagementPanel.add(btnLoadSequence);
        sequenceManagementPanel.add(btnSaveSequence);
        sequenceManagementPanel.add(btnManageSequences);

        topPanel.add(sequenceManagementPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        // 中部：任务队列列表
        JPanel queuePanel = new JPanel(new BorderLayout(5, 5));
        queuePanel.setBorder(BorderFactory.createTitledBorder("任务队列 (按顺序执行)"));

        queueListModel = new DefaultListModel<>();
        taskQueueList = new JList<>(queueListModel);
        taskQueueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        taskQueueList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        JScrollPane listScroll = new JScrollPane(taskQueueList);
        queuePanel.add(listScroll, BorderLayout.CENTER);

        panel.add(queuePanel, BorderLayout.CENTER);

        // 下部：队列操作按钮
        JPanel queueButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        queueButtonPanel.add(btnMoveUp);
        queueButtonPanel.add(btnMoveDown);
        queueButtonPanel.add(btnRemove);
        queueButtonPanel.add(btnClearQueue);

        panel.add(queueButtonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateSavedSequencesCombo() {
        savedSequencesCombo.removeAllItems();
        List<String> sequences = AppConfig.getAllSequenceNames();
        for (String sequence : sequences) {
            savedSequencesCombo.addItem(sequence);
        }
        if (!sequences.isEmpty()) {
            savedSequencesCombo.setSelectedIndex(0);
        }
    }

    private void registerHotkeys() {
        InputMap inputMap = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getRootPane().getActionMap();

        inputMap.clear();
        actionMap.clear();

        KeyStroke startHotkey = KeyStroke.getKeyStroke(AppConfig.getStartHotkey());
        inputMap.put(startHotkey, "startTaskAction");
        actionMap.put("startTaskAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isTaskRunning) {
                    handleStartAction();
                }
            }
        });

        KeyStroke stopHotkey = KeyStroke.getKeyStroke(AppConfig.getStopHotkey());
        inputMap.put(stopHotkey, "stopTaskAction");
        actionMap.put("stopTaskAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isTaskRunning) {
                    handleStopAction();
                }
            }
        });
    }

    private void updateButtonStates() {
        // 更新开始/停止按钮
        btnStart.setEnabled(!isTaskRunning);
        btnStop.setEnabled(isTaskRunning);

        // 更新按钮文本
        if (isTaskRunning) {
            btnStart.setText("开始执行(" + AppConfig.getStartHotkey() + ")");
            btnStop.setText("停止执行(" + AppConfig.getStopHotkey() + ")");
        } else {
            btnStart.setText("开始执行(" + AppConfig.getStartHotkey() + ")");
            btnStop.setText("停止执行(" + AppConfig.getStopHotkey() + ")");
        }

        // 更新队列操作按钮状态
        if (taskQueueList != null && queueListModel != null) {
            int selectedIndex = taskQueueList.getSelectedIndex();
            boolean hasSelection = selectedIndex != -1;
            boolean hasItems = !queueListModel.isEmpty();

            btnMoveUp.setEnabled(hasSelection && selectedIndex > 0);
            btnMoveDown.setEnabled(hasSelection && selectedIndex < queueListModel.size() - 1);
            btnRemove.setEnabled(hasSelection);
            btnClearQueue.setEnabled(hasItems);
            btnAddToQueue.setEnabled(!isTaskRunning);
            btnSaveSequence.setEnabled(hasItems && !isTaskRunning);
            btnLoadSequence.setEnabled(!isTaskRunning);
            btnManageSequences.setEnabled(!isTaskRunning);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == btnStart) {
            handleStartAction();
        } else if (source == btnStop) {
            handleStopAction();
        } else if (source == btnSettings) {
            handleSettingsAction();
        } else if (source == btnAddToQueue) {
            handleAddToQueue();
        } else if (source == btnClearQueue) {
            handleClearQueue();
        } else if (source == btnMoveUp) {
            handleMoveUp();
        } else if (source == btnMoveDown) {
            handleMoveDown();
        } else if (source == btnRemove) {
            handleRemove();
        } else if (source == btnSaveSequence) {
            handleSaveSequence();
        } else if (source == btnLoadSequence) {
            handleLoadSequence();
        } else if (source == btnManageSequences) {
            handleManageSequences();
        }
    }

    private void handleStartAction() {
        if (CLibrary.INSTANCE.IsDeviceConnected() > 0) {
            JTabbedPane tabs = (JTabbedPane) getContentPane().getComponent(0);
            int selectedTab = tabs.getSelectedIndex();

            if (selectedTab == 0) {
                // 单任务模式
                String selectedTask = (String) singleTaskComboBox.getSelectedItem();
                if (selectedTask != null) {
                    isTaskRunning = true;
                    updateButtonStates();
                    LogUtils.writeLog(logArea, "开始执行单任务: " + selectedTask);
                    taskExecutor.executeSingleTask(selectedTask);
                }
            } else {
                // 顺序任务模式
                if (queueListModel.isEmpty()) {
                    LogUtils.writeLog(logArea, "请先添加任务到队列");
                    return;
                }

                List<String> tasks = new ArrayList<>();
                for (int i = 0; i < queueListModel.size(); i++) {
                    tasks.add(queueListModel.getElementAt(i));
                }

                isTaskRunning = true;
                updateButtonStates();
                LogUtils.writeLog(logArea, "开始执行顺序任务，共 " + tasks.size() + " 个任务");
                LogUtils.writeLog(logArea, "执行顺序: " + String.join(" → ", tasks));
                taskExecutor.executeTaskSequence(tasks);
            }
        } else {
            LogUtils.writeLog(logArea, "设备未连接，无法执行任务");
        }
    }

    private void handleStopAction() {
        LogUtils.writeLog(logArea, "正在停止当前任务...");
        taskExecutor.cancelCurrentTask();
        isTaskRunning = false;
        updateButtonStates();
    }

    private void handleAddToQueue() {
        String selectedTask = (String) sequentialTaskComboBox.getSelectedItem();
        if (selectedTask != null) {
            queueListModel.addElement(selectedTask);
            updateButtonStates();
            LogUtils.writeLog(logArea, "已添加任务到队列: " + selectedTask);
        }
    }

    private void handleClearQueue() {
        if (queueListModel.isEmpty()) {
            LogUtils.writeLog(logArea, "任务队列已经是空的");
            return;
        }

        queueListModel.clear();
        updateButtonStates();
        LogUtils.writeLog(logArea, "已清空任务队列");
    }

    private void handleMoveUp() {
        int selectedIndex = taskQueueList.getSelectedIndex();
        if (selectedIndex > 0) {
            String task = queueListModel.remove(selectedIndex);
            queueListModel.add(selectedIndex - 1, task);
            taskQueueList.setSelectedIndex(selectedIndex - 1);
            updateButtonStates();
            LogUtils.writeLog(logArea, "已将任务上移: " + task);
        }
    }

    private void handleMoveDown() {
        int selectedIndex = taskQueueList.getSelectedIndex();
        if (selectedIndex < queueListModel.size() - 1) {
            String task = queueListModel.remove(selectedIndex);
            queueListModel.add(selectedIndex + 1, task);
            taskQueueList.setSelectedIndex(selectedIndex + 1);
            updateButtonStates();
            LogUtils.writeLog(logArea, "已将任务下移: " + task);
        }
    }

    private void handleRemove() {
        int selectedIndex = taskQueueList.getSelectedIndex();
        if (selectedIndex != -1) {
            String removedTask = queueListModel.remove(selectedIndex);
            updateButtonStates();
            LogUtils.writeLog(logArea, "已从队列移除任务: " + removedTask);

            // 如果移除后列表还有项目，保持选择状态
            if (!queueListModel.isEmpty()) {
                if (selectedIndex >= queueListModel.size()) {
                    taskQueueList.setSelectedIndex(queueListModel.size() - 1);
                } else {
                    taskQueueList.setSelectedIndex(selectedIndex);
                }
            }
        }
    }

    private void handleSaveSequence() {
        if (queueListModel.isEmpty()) {
            LogUtils.writeLog(logArea, "队列为空，无法保存");
            return;
        }

        String sequenceName = JOptionPane.showInputDialog(this, "请输入序列名称:", "保存任务序列", JOptionPane.QUESTION_MESSAGE);
        if (sequenceName != null && !sequenceName.trim().isEmpty()) {
            sequenceName = sequenceName.trim();

            // 检查是否已存在
            if (AppConfig.sequenceExists(sequenceName)) {
                int result = JOptionPane.showConfirmDialog(
                        this,
                        "序列 '" + sequenceName + "' 已存在，是否覆盖？",
                        "确认覆盖",
                        JOptionPane.YES_NO_OPTION
                );
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            List<String> tasks = new ArrayList<>();
            for (int i = 0; i < queueListModel.size(); i++) {
                tasks.add(queueListModel.getElementAt(i));
            }

            AppConfig.saveTaskSequence(sequenceName, tasks);
            updateSavedSequencesCombo();
            LogUtils.writeLog(logArea, "已保存任务序列: " + sequenceName);
        }
    }

    private void handleLoadSequence() {
        String selectedSequence = (String) savedSequencesCombo.getSelectedItem();
        if (selectedSequence != null) {
            List<String> tasks = AppConfig.loadTaskSequence(selectedSequence);
            if (!tasks.isEmpty()) {
                // 确认是否覆盖当前队列
                if (!queueListModel.isEmpty()) {
                    int result = JOptionPane.showConfirmDialog(
                            this,
                            "加载序列将覆盖当前队列，是否继续？",
                            "确认加载",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                queueListModel.clear();
                for (String task : tasks) {
                    queueListModel.addElement(task);
                }
                updateButtonStates();
                LogUtils.writeLog(logArea, "已加载任务序列: " + selectedSequence);
                LogUtils.writeLog(logArea, "加载的任务: " + String.join(" → ", tasks));
            } else {
                LogUtils.writeLog(logArea, "加载序列失败或序列为空: " + selectedSequence);
            }
        }
    }

    private void handleManageSequences() {
        List<String> sequences = AppConfig.getAllSequenceNames();
        if (sequences.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有保存的任务序列", "管理序列", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 创建管理对话框
        JDialog manageDialog = new JDialog(this, "管理任务序列", true);
        manageDialog.setSize(400, 300);
        manageDialog.setLocationRelativeTo(this);
        manageDialog.setLayout(new BorderLayout());

        // 序列列表
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String sequence : sequences) {
            listModel.addElement(sequence);
        }
        JList<String> sequenceList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(sequenceList);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton btnDelete = new JButton("删除选中");
        JButton btnRename = new JButton("重命名");
        JButton btnClose = new JButton("关闭");

        btnDelete.addActionListener(e -> {
            String selected = sequenceList.getSelectedValue();
            if (selected != null) {
                int result = JOptionPane.showConfirmDialog(
                        manageDialog,
                        "确定要删除序列 '" + selected + "' 吗？",
                        "确认删除",
                        JOptionPane.YES_NO_OPTION
                );
                if (result == JOptionPane.YES_OPTION) {
                    if (AppConfig.deleteTaskSequence(selected)) {
                        listModel.removeElement(selected);
                        updateSavedSequencesCombo();
                        LogUtils.writeLog(logArea, "已删除任务序列: " + selected);
                    } else {
                        LogUtils.writeLog(logArea, "删除序列失败: " + selected);
                    }
                }
            }
        });

        btnRename.addActionListener(e -> {
            String selected = sequenceList.getSelectedValue();
            if (selected != null) {
                String newName = JOptionPane.showInputDialog(
                        manageDialog,
                        "请输入新的序列名称:",
                        "重命名序列",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (newName != null && !newName.trim().isEmpty()) {
                    newName = newName.trim();
                    if (!newName.equals(selected)) {
                        if (AppConfig.sequenceExists(newName)) {
                            JOptionPane.showMessageDialog(manageDialog, "序列名称已存在", "错误", JOptionPane.ERROR_MESSAGE);
                        } else {
                            List<String> tasks = AppConfig.loadTaskSequence(selected);
                            AppConfig.saveTaskSequence(newName, tasks);
                            AppConfig.deleteTaskSequence(selected);
                            listModel.removeElement(selected);
                            listModel.addElement(newName);
                            updateSavedSequencesCombo();
                            LogUtils.writeLog(logArea, "已重命名序列: " + selected + " → " + newName);
                        }
                    }
                }
            }
        });

        btnClose.addActionListener(e -> manageDialog.dispose());

        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRename);
        buttonPanel.add(btnClose);

        manageDialog.add(scrollPane, BorderLayout.CENTER);
        manageDialog.add(buttonPanel, BorderLayout.SOUTH);
        manageDialog.setVisible(true);
    }

    private void handleSettingsAction() {
        SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.setVisible(true);

        registerHotkeys();
        updateButtonStates();

        LogUtils.writeLog(logArea, "程序设置已更新");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new MainJpanel());
    }
}


