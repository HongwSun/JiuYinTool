package com.jiuyin.ui;

import com.jiuyin.config.AppConfig;
import com.jiuyin.util.LogUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务队列面板
 */
public class TaskQueuePanel extends JPanel {
    private final JComboBox<String> sequentialTaskComboBox;
    private final JComboBox<String> savedSequencesCombo;
    private final JButton btnAddToQueue;
    private final JButton btnClearQueue;
    private final JButton btnMoveUp;
    private final JButton btnMoveDown;
    private final JButton btnRemove;
    private final JButton btnSaveSequence;
    private final JButton btnLoadSequence;
    private final JButton btnManageSequences;

    private JList<String> taskQueueList;
    private DefaultListModel<String> queueListModel;

    public TaskQueuePanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 上部：任务选择和添加
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        JPanel taskSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        taskSelectPanel.add(new JLabel("选择任务:"));

        sequentialTaskComboBox = new JComboBox<>();
        sequentialTaskComboBox.addItem("团练任务");
        taskSelectPanel.add(sequentialTaskComboBox);

        btnAddToQueue = new JButton("添加到队列");
        taskSelectPanel.add(btnAddToQueue);

        topPanel.add(taskSelectPanel, BorderLayout.NORTH);

        // 序列管理面板
        JPanel sequenceManagementPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sequenceManagementPanel.add(new JLabel("保存的序列:"));

        savedSequencesCombo = new JComboBox<>();
        updateSavedSequencesCombo();
        sequenceManagementPanel.add(savedSequencesCombo);

        btnLoadSequence = new JButton("加载序列");
        sequenceManagementPanel.add(btnLoadSequence);

        btnSaveSequence = new JButton("保存序列");
        sequenceManagementPanel.add(btnSaveSequence);

        btnManageSequences = new JButton("管理序列");
        sequenceManagementPanel.add(btnManageSequences);

        topPanel.add(sequenceManagementPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // 中部：任务队列列表
        JPanel queuePanel = new JPanel(new BorderLayout(5, 5));
        queuePanel.setBorder(BorderFactory.createTitledBorder("任务队列 (按顺序执行)"));

        queueListModel = new DefaultListModel<>();
        taskQueueList = new JList<>(queueListModel);
        taskQueueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        taskQueueList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates(false);
            }
        });

        JScrollPane listScroll = new JScrollPane(taskQueueList);
        queuePanel.add(listScroll, BorderLayout.CENTER);

        add(queuePanel, BorderLayout.CENTER);

        // 下部：队列操作按钮
        JPanel queueButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnMoveUp = new JButton("上移");
        queueButtonPanel.add(btnMoveUp);

        btnMoveDown = new JButton("下移");
        queueButtonPanel.add(btnMoveDown);

        btnRemove = new JButton("移除");
        queueButtonPanel.add(btnRemove);

        btnClearQueue = new JButton("清空队列");
        queueButtonPanel.add(btnClearQueue);

        add(queueButtonPanel, BorderLayout.SOUTH);

        // 初始状态
        updateButtonStates(false);
    }

    public void updateSavedSequencesCombo() {
        savedSequencesCombo.removeAllItems();
        List<String> sequences = AppConfig.getAllSequenceNames();
        for (String sequence : sequences) {
            savedSequencesCombo.addItem(sequence);
        }
        if (!sequences.isEmpty()) {
            savedSequencesCombo.setSelectedIndex(0);
        }
    }

    public void updateButtonStates(boolean isTaskRunning) {
        int selectedIndex = taskQueueList.getSelectedIndex();
        boolean hasSelection = selectedIndex != -1;
        boolean hasItems = !queueListModel.isEmpty();

        btnMoveUp.setEnabled(!isTaskRunning && hasSelection && selectedIndex > 0);
        btnMoveDown.setEnabled(!isTaskRunning && hasSelection && selectedIndex < queueListModel.size() - 1);
        btnRemove.setEnabled(!isTaskRunning && hasSelection);
        btnClearQueue.setEnabled(!isTaskRunning && hasItems);
        btnAddToQueue.setEnabled(!isTaskRunning);
        btnSaveSequence.setEnabled(!isTaskRunning && hasItems);
        btnLoadSequence.setEnabled(!isTaskRunning);
        btnManageSequences.setEnabled(!isTaskRunning);
    }

    public void addToQueue() {
        String taskType = (String) sequentialTaskComboBox.getSelectedItem();
        if (taskType != null) {
            queueListModel.addElement(taskType);
        }
    }

    public void clearQueue() {
        queueListModel.clear();
    }

    public void moveUp() {
        int selectedIndex = taskQueueList.getSelectedIndex();
        if (selectedIndex > 0) {
            String task = queueListModel.remove(selectedIndex);
            queueListModel.add(selectedIndex - 1, task);
            taskQueueList.setSelectedIndex(selectedIndex - 1);
        }
    }

    public void moveDown() {
        int selectedIndex = taskQueueList.getSelectedIndex();
        if (selectedIndex < queueListModel.size() - 1) {
            String task = queueListModel.remove(selectedIndex);
            queueListModel.add(selectedIndex + 1, task);
            taskQueueList.setSelectedIndex(selectedIndex + 1);
        }
    }

    public void removeSelected() {
        int selectedIndex = taskQueueList.getSelectedIndex();
        if (selectedIndex != -1) {
            queueListModel.remove(selectedIndex);
        }
    }

    public void saveSequence(JTextArea logArea) {
        if (queueListModel.isEmpty()) {
            LogUtils.writeLog(logArea, "队列为空，无法保存");
            return;
        }

        String sequenceName = JOptionPane.showInputDialog(this, "输入序列名称:", "保存序列", JOptionPane.QUESTION_MESSAGE);
        if (sequenceName != null && !sequenceName.trim().isEmpty()) {
            List<String> tasks = new ArrayList<>();
            for (int i = 0; i < queueListModel.size(); i++) {
                tasks.add(queueListModel.getElementAt(i));
            }
            AppConfig.saveTaskSequence(sequenceName, tasks);
            updateSavedSequencesCombo();
            LogUtils.writeLog(logArea, "序列 '" + sequenceName + "' 保存成功");
        }
    }

    public void loadSequence(JTextArea logArea) {
        String sequenceName = (String) savedSequencesCombo.getSelectedItem();
        if (sequenceName != null) {
            List<String> tasks = AppConfig.loadTaskSequence(sequenceName);
            if (tasks != null) {
                queueListModel.clear();
                for (String task : tasks) {
                    queueListModel.addElement(task);
                }
                LogUtils.writeLog(logArea, "序列 '" + sequenceName + "' 加载成功");
            }
        }
    }

    public boolean isQueueEmpty() {
        return queueListModel.isEmpty();
    }

    public List<String> getTaskSequence() {
        List<String> taskSequence = new ArrayList<>();
        for (int i = 0; i < queueListModel.size(); i++) {
            taskSequence.add(queueListModel.getElementAt(i));
        }
        return taskSequence;
    }

    // Getters for buttons
    public JButton getBtnAddToQueue() { return btnAddToQueue; }
    public JButton getBtnClearQueue() { return btnClearQueue; }
    public JButton getBtnMoveUp() { return btnMoveUp; }
    public JButton getBtnMoveDown() { return btnMoveDown; }
    public JButton getBtnRemove() { return btnRemove; }
    public JButton getBtnSaveSequence() { return btnSaveSequence; }
    public JButton getBtnLoadSequence() { return btnLoadSequence; }
    public JButton getBtnManageSequences() { return btnManageSequences; }
}