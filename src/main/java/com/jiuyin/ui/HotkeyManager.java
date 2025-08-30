package com.jiuyin.ui;

import com.jiuyin.config.AppConfig;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 *热键管理器
 */
public class HotkeyManager {
    private final MainFrame mainFrame;

    public HotkeyManager(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public void registerHotkeys() {
        InputMap inputMap = mainFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mainFrame.getRootPane().getActionMap();

        inputMap.clear();
        actionMap.clear();

        // 开始热键
        javax.swing.KeyStroke startHotkey = javax.swing.KeyStroke.getKeyStroke(AppConfig.getStartHotkey());
        inputMap.put(startHotkey, "startTaskAction");
        actionMap.put("startTaskAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!mainFrame.isTaskRunning()) {
                    mainFrame.handleStartAction();
                }
            }
        });

        // 停止热键
        javax.swing.KeyStroke stopHotkey = javax.swing.KeyStroke.getKeyStroke(AppConfig.getStopHotkey());
        inputMap.put(stopHotkey, "stopTaskAction");
        actionMap.put("stopTaskAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainFrame.isTaskRunning()) {
                    mainFrame.handleStopAction();
                }
            }
        });
    }
}