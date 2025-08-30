package com.jiuyin.ui;

import com.jiuyin.model.SelectedWindow;
import com.jiuyin.function.window.WindowHandle;
import com.jiuyin.util.LogUtils;
import com.jiuyin.function.window.WindowCapture;
import com.sun.jna.platform.win32.WinDef;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 窗口选择面板
 */
public class WindowSelectionPanel extends JPanel {
    private final JComboBox<String> windowComboBox;
    private final JButton btnRefreshWindows;
    private final JButton btnSelectWindow;

    private List<WinDef.HWND> gameWindows = new ArrayList<>();
    private WindowCapture windowCapture = new WindowCapture();

    public WindowSelectionPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("窗口选择"));

        add(new JLabel("游戏窗口:"));

        windowComboBox = new JComboBox<>();
        windowComboBox.setPreferredSize(new Dimension(300, 25));
        add(windowComboBox);

        btnRefreshWindows = new JButton("刷新窗口");
        add(btnRefreshWindows);

        btnSelectWindow = new JButton("选择窗口");
        add(btnSelectWindow);
    }

    public void refreshWindowList() {
        gameWindows = WindowHandle.getAllGameWindows();
        windowComboBox.removeAllItems();

        if (gameWindows.isEmpty()) {
            windowComboBox.addItem("未找到游戏窗口");
            btnSelectWindow.setEnabled(false);
        } else {
            for (int i = 0; i < gameWindows.size(); i++) {
                WinDef.HWND hwnd = gameWindows.get(i);
                String title = windowCapture.getWindowTitle(hwnd);
                windowComboBox.addItem((i + 1) + ": " + (title.isEmpty() ? "未知窗口" : title));
            }
            btnSelectWindow.setEnabled(true);

            // 默认选中第一个窗口
            windowComboBox.setSelectedIndex(0);
            SelectedWindow.setSelectedWindowHandle(gameWindows.get(0));
        }
    }

    public void selectWindow(JTextArea logArea) {
        int selectedIndex = windowComboBox.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < gameWindows.size()) {
            WinDef.HWND selectedWindow = gameWindows.get(selectedIndex);
            SelectedWindow.setSelectedWindowHandle(selectedWindow);

            String title = windowCapture.getWindowTitle(selectedWindow);
            LogUtils.writeLog(logArea, "已选择窗口: " + (title.isEmpty() ? "未知窗口" : title));
        }
    }

    public WinDef.HWND getSelectedWindowHandle() {
        return SelectedWindow.getSelectedWindowHandle();
    }

    public JButton getBtnRefreshWindows() {
        return btnRefreshWindows;
    }

    public JButton getBtnSelectWindow() {
        return btnSelectWindow;
    }

    public JComboBox<String> getWindowComboBox() {
        return windowComboBox;
    }
}