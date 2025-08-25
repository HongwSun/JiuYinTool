package com.jiuyin.ui;

import com.jiuyin.config.AppConfig;
import com.jiuyin.util.LogUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 管理任务序列面板
 */
public class SequenceManagerDialog extends JDialog {
    private final JTextArea logArea;

    public SequenceManagerDialog(JFrame parent, JTextArea logArea) {
        super(parent, "管理任务序列", true);
        this.logArea = logArea;

        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initializeComponents();
    }

    private void initializeComponents() {
        List<String> sequences = AppConfig.getAllSequenceNames();

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

        btnDelete.addActionListener(e -> deleteSequence(sequenceList, listModel));
        btnRename.addActionListener(e -> renameSequence(sequenceList, listModel));
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnDelete);
        buttonPanel.add(btnRename);
        buttonPanel.add(btnClose);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void deleteSequence(JList<String> sequenceList, DefaultListModel<String> listModel) {
        String selected = sequenceList.getSelectedValue();
        if (selected != null) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "确定要删除序列 '" + selected + "' 吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                if (AppConfig.deleteTaskSequence(selected)) {
                    listModel.removeElement(selected);
                    LogUtils.writeLog(logArea, "已删除任务序列: " + selected);
                } else {
                    LogUtils.writeLog(logArea, "删除序列失败: " + selected);
                }
            }
        }
    }

    private void renameSequence(JList<String> sequenceList, DefaultListModel<String> listModel) {
        String selected = sequenceList.getSelectedValue();
        if (selected != null) {
            String newName = JOptionPane.showInputDialog(
                    this,
                    "请输入新的序列名称:",
                    "重命名序列",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (newName != null && !newName.trim().isEmpty()) {
                newName = newName.trim();
                if (!newName.equals(selected)) {
                    if (AppConfig.sequenceExists(newName)) {
                        JOptionPane.showMessageDialog(this, "序列名称已存在", "错误", JOptionPane.ERROR_MESSAGE);
                    } else {
                        List<String> tasks = AppConfig.loadTaskSequence(selected);
                        AppConfig.saveTaskSequence(newName, tasks);
                        AppConfig.deleteTaskSequence(selected);
                        listModel.removeElement(selected);
                        listModel.addElement(newName);
                        LogUtils.writeLog(logArea, "已重命名序列: " + selected + " → " + newName);
                    }
                }
            }
        }
    }
}