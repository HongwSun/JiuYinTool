package com.jiuyin.ui;

import com.jiuyin.config.AppConfig;

import javax.swing.*;
import java.awt.*;

/**
 * 右侧按钮面板
 */
public class ButtonPanel extends JPanel {
    private final JButton btnSettings = new JButton("程序设置");
    private final JButton btnStart = new JButton("开始执行");
    private final JButton btnStop = new JButton("停止执行");

    public ButtonPanel() {
        setLayout(new GridLayout(0, 1, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton infoButton = new JButton("目前只实现了团练按键");
        infoButton.setEnabled(false);
        add(infoButton);

        add(btnSettings);
        add(btnStart);
        add(btnStop);

        // 设置按钮的首选大小
        Dimension buttonSize = new Dimension(150, 30);
        btnSettings.setPreferredSize(buttonSize);
        btnStart.setPreferredSize(buttonSize);
        btnStop.setPreferredSize(buttonSize);
        infoButton.setPreferredSize(buttonSize);

        // 初始状态
        updateButtonStates(false, false);
    }

    public void updateButtonStates(boolean isTaskRunning, boolean hasSelectedWindow) {
        btnStart.setEnabled(!isTaskRunning && hasSelectedWindow);
        btnStop.setEnabled(isTaskRunning);

        // 更新按钮文本
        if (isTaskRunning) {
            btnStart.setText("开始执行(" + AppConfig.getStartHotkey() + ")");
            btnStop.setText("停止执行(" + AppConfig.getStopHotkey() + ")");
        } else {
            btnStart.setText("开始执行(" + AppConfig.getStartHotkey() + ")");
            btnStop.setText("停止执行(" + AppConfig.getStopHotkey() + ")");
        }
    }

    public JButton getBtnSettings() {
        return btnSettings;
    }

    public JButton getBtnStart() {
        return btnStart;
    }

    public JButton getBtnStop() {
        return btnStop;
    }
}