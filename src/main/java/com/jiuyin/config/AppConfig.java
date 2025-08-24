package com.jiuyin.config;

import java.io.*;
import java.util.*;

/**
 * 通用配置文件管理
 */
public class AppConfig {
    private static final String CONFIG_FILE = "app.config";
    private static final String SEQUENCES_DIR = "sequences/";
    private static final Properties properties = new Properties();

    // 默认配置值
    private static final String DEFAULT_START_HOTKEY = "F10";
    private static final String DEFAULT_STOP_HOTKEY = "F12";
    private static final String DEFAULT_MATCH_THRESHOLD = "0.8";
    private static final String DEFAULT_DETECTION_INTERVAL = "200";

    static {
        loadConfig();
        ensureSequencesDir();
    }

    /**
     * 确保序列目录存在
     */
    private static void ensureSequencesDir() {
        File dir = new File(SEQUENCES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 加载配置文件
     */
    private static void loadConfig() {
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException e) {
            // 文件不存在，使用默认值并创建配置文件
            setDefaultValues();
            saveConfig();
        }
    }

    /**
     * 设置默认值
     */
    private static void setDefaultValues() {
        properties.setProperty("hotkey.start", DEFAULT_START_HOTKEY);
        properties.setProperty("hotkey.stop", DEFAULT_STOP_HOTKEY);
        properties.setProperty("recognition.match_threshold", DEFAULT_MATCH_THRESHOLD);
        properties.setProperty("recognition.detection_interval", DEFAULT_DETECTION_INTERVAL);
    }

    /**
     * 保存配置到文件
     */
    public static void saveConfig() {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, "Application Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== 热键配置 ====================
    public static String getStartHotkey() {
        return properties.getProperty("hotkey.start", DEFAULT_START_HOTKEY);
    }

    public static String getStopHotkey() {
        return properties.getProperty("hotkey.stop", DEFAULT_STOP_HOTKEY);
    }

    public static void setStartHotkey(String hotkey) {
        properties.setProperty("hotkey.start", hotkey);
        saveConfig();
    }

    public static void setStopHotkey(String hotkey) {
        properties.setProperty("hotkey.stop", hotkey);
        saveConfig();
    }

    // ==================== 识别配置 ====================
    public static double getMatchThreshold() {
        return Double.parseDouble(properties.getProperty("recognition.match_threshold", DEFAULT_MATCH_THRESHOLD));
    }

    public static int getDetectionInterval() {
        return Integer.parseInt(properties.getProperty("recognition.detection_interval", DEFAULT_DETECTION_INTERVAL));
    }

    public static void setMatchThreshold(double value) {
        properties.setProperty("recognition.match_threshold", String.valueOf(value));
        saveConfig();
    }

    public static void setDetectionInterval(int value) {
        properties.setProperty("recognition.detection_interval", String.valueOf(value));
        saveConfig();
    }

    // ==================== 任务序列管理 ====================

    /**
     * 保存任务序列到文件
     */
    public static void saveTaskSequence(String sequenceName, List<String> tasks) {
        try {
            File file = new File(SEQUENCES_DIR + sequenceName + ".seq");
            try (PrintWriter writer = new PrintWriter(file)) {
                for (String task : tasks) {
                    writer.println(task);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载任务序列
     */
    public static List<String> loadTaskSequence(String sequenceName) {
        List<String> tasks = new ArrayList<>();
        try {
            File file = new File(SEQUENCES_DIR + sequenceName + ".seq");
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            tasks.add(line.trim());
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    /**
     * 获取所有保存的任务序列名称
     */
    public static List<String> getAllSequenceNames() {
        List<String> names = new ArrayList<>();
        File dir = new File(SEQUENCES_DIR);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".seq"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    names.add(name.substring(0, name.length() - 4)); // 移除 .seq 后缀
                }
            }
        }
        Collections.sort(names);
        return names;
    }

    /**
     * 删除任务序列
     */
    public static boolean deleteTaskSequence(String sequenceName) {
        File file = new File(SEQUENCES_DIR + sequenceName + ".seq");
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 检查序列是否存在
     */
    public static boolean sequenceExists(String sequenceName) {
        File file = new File(SEQUENCES_DIR + sequenceName + ".seq");
        return file.exists();
    }

    // ==================== 通用方法 ====================
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        saveConfig();
    }

    /**
     * 重置所有设置为默认值
     */
    public static void resetToDefaults() {
        setDefaultValues();
        saveConfig();
    }

    /**
     * 获取所有配置（用于显示）
     */
    public static Properties getAllProperties() {
        return new Properties(properties);
    }
}

