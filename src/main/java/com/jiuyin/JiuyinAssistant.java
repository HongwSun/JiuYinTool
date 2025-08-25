package com.jiuyin;

import com.jiuyin.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class JiuyinAssistant {
    public static void main(String[] args) {
        /* 统一字体，避免中文乱码 */
        String fontName = "Microsoft YaHei";
        UIManager.put("Label.font",    new Font(fontName, Font.PLAIN, 14));
        UIManager.put("Button.font",   new Font(fontName, Font.PLAIN, 14));
        UIManager.put("CheckBox.font", new Font(fontName, Font.PLAIN, 14));
        UIManager.put("TabbedPane.font", new Font(fontName, Font.PLAIN, 14));

        SwingUtilities.invokeLater(MainFrame::new);
    }
}
