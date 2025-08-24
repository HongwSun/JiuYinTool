package com.jiuyin.ui;

import com.jiuyin.config.AppConfig;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 通用设置对话框
 */
public class SettingsDialog extends JDialog {
    private JTabbedPane tabbedPane;
    private JButton saveButton;
    private JButton resetButton;
    private JButton cancelButton;

    // 热键选项卡组件
    private JTextField startHotkeyField;
    private JTextField stopHotkeyField;

    // 识别配置选项卡组件
    private JTextField searchStartYField;
    private JTextField searchHeightField;
    private JTextField matchThresholdField;
    private JTextField detectionIntervalField;

    public SettingsDialog(JFrame parent) {
        super(parent, "程序设置", true);
        initializeUI();
        loadCurrentSettings();
    }

    private void initializeUI() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        // 选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("热键设置", createHotkeyPanel());
        tabbedPane.addTab("识别配置", createRecognitionPanel());
        tabbedPane.addTab("关于", createOtherSettingsPanel());

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("保存");
        resetButton = new JButton("恢复默认");
        cancelButton = new JButton("取消");

        saveButton.addActionListener(this::saveSettings);
        resetButton.addActionListener(this::resetSettings);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 创建热键设置面板
     */
    private JPanel createHotkeyPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("开始热键:"));
        startHotkeyField = createHotkeyTextField();
        panel.add(startHotkeyField);

        panel.add(new JLabel("停止热键:"));
        stopHotkeyField = createHotkeyTextField();
        panel.add(stopHotkeyField);

        panel.add(new JLabel("提示:"));
        panel.add(new JLabel("按F1-F12键设置热键"));

        return panel;
    }

    /**
     * 创建识别配置面板
     */
    private JPanel createRecognitionPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        panel.add(new JLabel("匹配阈值(0-1):"));
        matchThresholdField = new JTextField();
        panel.add(matchThresholdField);

        panel.add(new JLabel("检测间隔(ms):"));
        detectionIntervalField = new JTextField();
        panel.add(detectionIntervalField);

        return panel;
    }

    /**
     * 创建其他设置面板
     */
    private JPanel createOtherSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea infoArea = new JTextArea(
                "配置文件位置: app.config\n\n" +
                        "配置说明:\n" +
                        "• 热键设置: 程序功能快捷键\n" +
                        "• 识别配置: 图像识别相关参数\n" +
                        "• 修改后需要重启程序生效\n\n" +
                        "默认热键: F10(开始), F12(停止)"
        );
        infoArea.setEditable(false);
        infoArea.setBackground(panel.getBackground());

        panel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建热键输入框
     */
    private JTextField createHotkeyTextField() {
        JTextField field = new JTextField();
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String keyText = KeyEvent.getKeyText(e.getKeyCode());
                if (keyText.startsWith("F") && keyText.length() > 1) {
                    field.setText(keyText);
                }
                e.consume();
            }
        });
        return field;
    }

    /**
     * 加载当前设置
     */
    private void loadCurrentSettings() {
        // 热键设置
        startHotkeyField.setText(AppConfig.getStartHotkey());
        stopHotkeyField.setText(AppConfig.getStopHotkey());

        // 识别配置
        matchThresholdField.setText(String.valueOf(AppConfig.getMatchThreshold()));
        detectionIntervalField.setText(String.valueOf(AppConfig.getDetectionInterval()));
    }

    /**
     * 保存设置
     */
    private void saveSettings(ActionEvent e) {
        try {
            // 保存热键设置
            if (isValidHotkey(startHotkeyField.getText())) {
                AppConfig.setStartHotkey(startHotkeyField.getText());
            }
            if (isValidHotkey(stopHotkeyField.getText())) {
                AppConfig.setStopHotkey(stopHotkeyField.getText());
            }

            // 保存识别配置
            AppConfig.setMatchThreshold(Double.parseDouble(matchThresholdField.getText()));
            AppConfig.setDetectionInterval(Integer.parseInt(detectionIntervalField.getText()));

            JOptionPane.showMessageDialog(this, "设置已保存！部分设置需要重启程序生效。", "成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 恢复默认设置
     */
    private void resetSettings(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(this,
                "确定要恢复所有默认设置吗？", "确认", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            AppConfig.resetToDefaults();
            loadCurrentSettings();
            JOptionPane.showMessageDialog(this, "已恢复默认设置！", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 验证热键格式
     */
    private boolean isValidHotkey(String hotkey) {
        return hotkey.matches("F[1-9]|F1[0-2]");
    }
}