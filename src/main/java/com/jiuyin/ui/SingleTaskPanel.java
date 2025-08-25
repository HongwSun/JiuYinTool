package com.jiuyin.ui;

import javax.swing.*;
import java.awt.*;

public class SingleTaskPanel extends JPanel {
    private final JComboBox<String> singleTaskComboBox;

    public SingleTaskPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 任务选择
        JPanel taskSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        taskSelectPanel.add(new JLabel("选择任务:"));

        singleTaskComboBox = new JComboBox<>();
        singleTaskComboBox.addItem("团练任务");
        singleTaskComboBox.addItem("种地任务");
        taskSelectPanel.add(singleTaskComboBox);

        add(taskSelectPanel, BorderLayout.NORTH);

        // 说明文本
        JTextArea description = new JTextArea("在此模式下每次只执行一个选定的任务。\n适合单独完成某个特定任务。");
        description.setEditable(false);
        description.setBackground(getBackground());
        description.setFont(new Font("宋体", Font.PLAIN, 12));
        add(description, BorderLayout.CENTER);
    }

    public String getSelectedTask() {
        return (String) singleTaskComboBox.getSelectedItem();
    }

    public JComboBox<String> getSingleTaskComboBox() {
        return singleTaskComboBox;
    }
}