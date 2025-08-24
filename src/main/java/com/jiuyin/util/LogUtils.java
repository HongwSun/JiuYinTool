package com.jiuyin.util;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志工具类：统一处理日志的格式化、输出和滚动逻辑
 */
public class LogUtils {
    private LogUtils() {}
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**
     * 向指定的文本域写入日志（带时间戳，自动滚动到最新日志）
     * @param logArea 日志输出目标（JTextArea）
     * @param logContent 日志内容（无需包含时间，工具类自动添加）
     */
    public static void writeLog(JTextArea logArea, String logContent) {
        if (logArea == null || logContent == null) {
            throw new IllegalArgumentException("日志输出目标（logArea）和日志内容（logContent）不可为null！");
        }
        String formattedDate = LocalDateTime.now().format(DATE_FORMATTER);
        String formattedLog = formattedDate + logContent;

        SwingUtilities.invokeLater(() -> {
            logArea.append("\n" + formattedLog);
            scrollToLatestLog(logArea); // 自动滚动到最新日志
        });
    }

    /**
     * 单独提取「滚动到最新日志」的逻辑（单一职责：便于复用和修改）
     */
    private static void scrollToLatestLog(JTextArea logArea) {
        try {
            // 获取文档总长度（最新日志的末尾位置）
            int latestPosition = logArea.getDocument().getLength();
            // 定位光标到末尾（带动滚动条同步滚动）
            logArea.setCaretPosition(latestPosition);
        } catch (Exception e) {
            // 异常兜底：避免日志滚动失败导致整个程序崩溃
            e.printStackTrace();
            writeLog(logArea, "日志滚动失败：" + e.getMessage());
        }
    }

    // （可选）扩展：清空日志的方法（如需清空功能，直接调用此方法）
    public static void clearLog(JTextArea logArea) {
        if (logArea == null) {
            throw new IllegalArgumentException("日志输出目标（logArea）不可为null！");
        }
        SwingUtilities.invokeLater(() -> logArea.setText(""));
    }
}