package com.jiuyin.ui;

import javax.swing.*;
import java.awt.*;

/**
 * 日志面板
 */
public class LogPanel extends JPanel {
    private final JTextArea logArea;

    public LogPanel() {
        setLayout(new BorderLayout());

        logArea = new JTextArea(8, 0);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("日志"));

        add(logScroll, BorderLayout.CENTER);
    }

    public JTextArea getLogArea() {
        return logArea;
    }

    public void clearLog() {
        logArea.setText("");
    }

    public void appendLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}