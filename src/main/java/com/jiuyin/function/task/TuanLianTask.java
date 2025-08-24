package com.jiuyin.function.task;

import com.jiuyin.config.AppConfig;
import com.jiuyin.config.KeyConfig;
import com.jiuyin.nativeapi.CLibrary;
import com.jiuyin.util.ImageRecognizer;
import com.jiuyin.util.WindowCapture;
import com.sun.jna.platform.win32.WinDef;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.jiuyin.function.window.WindowHandle.getHwnd;

/**
 * 团练授业任务实现类
 */
public class TuanLianTask extends BaseTask {
    private final WindowCapture windowCapture = new WindowCapture();
    private final ImageRecognizer imageRecognizer = new ImageRecognizer();
    private final Map<String, BufferedImage> templateCache = new HashMap<>();
    private static final double MATCH_THRESHOLD = AppConfig.getMatchThreshold();
    private static final long DEFAULT_DETECTION_INTERVAL = AppConfig.getDetectionInterval();

    public TuanLianTask(JTextArea logArea) {
        super(logArea);
    }

    @Override
    public String getName() {
        return "团练授业";
    }

    @Override
    public void execute() throws InterruptedException {
        running = true;
        completed = false;

        try {
            WinDef.HWND hwnd = getHwnd();
            if (hwnd == null || !windowCapture.isWindowValid(hwnd)) {
                log("窗口无效");
                return;
            }
            log("找到窗口: " + windowCapture.getWindowTitle(hwnd));
            preloadTemplates();


            log("开始监控按键序列...");
            monitorIcons(hwnd);

            completed = true;
            log("任务完成");
        } finally {
            running = false;
        }
    }

    private void preloadTemplates() {
        for (Map.Entry<String, String> entry : KeyConfig.KEY_TEMPLATES.entrySet()) {
            String key = entry.getKey();
            String templatePath = entry.getValue();

            try {
                BufferedImage template = ImageIO.read(new File(templatePath));
                if (template != null) {
                    templateCache.put(key, template);
                    log("加载模板成功: " + key);
                }
            } catch (IOException e) {
                log("模版加载失败: " + key + " - " + e.getMessage());
            }
        }
    }

    /**
     * 监控按键
     */
    private void monitorIcons(WinDef.HWND hwnd) throws InterruptedException {
        try {

            while (running) { // 限制检测次数，避免无限循环
                BufferedImage screen = windowCapture.captureClientArea(hwnd);
                if (screen == null) {
                    log("截图失败，等待重试...");
                    Thread.sleep(DEFAULT_DETECTION_INTERVAL);
                    continue;
                }

                String sequence = recognizeIconsWithHighThreshold(screen);
                if (!sequence.isEmpty()) {
                    log("识别到按键序列: " + sequence);
                    // 调用幽灵键鼠进行按键操作
                    CLibrary.INSTANCE.InputString(sequence);
                    // 按键后等待一段时间
                    Thread.sleep(1000);
                } else {
                    log("未识别到按键序列");
                }
                Thread.sleep(500);

            }
        } catch (Exception e) {
            log("监控过程中发生异常: " + e.getMessage());
        }
    }

    /**
     * 识别图标
     */
    private String recognizeIconsWithHighThreshold(BufferedImage screen) {
        List<ImageRecognizer.MatchResult> allMatches = new ArrayList<>();

        int startY = screen.getHeight() / 2;
        int height = screen.getHeight() / 2;

        for (Map.Entry<String, BufferedImage> entry : templateCache.entrySet()) {
            String key = entry.getKey();
            BufferedImage template = entry.getValue();

            List<ImageRecognizer.MatchResult> matches = imageRecognizer.findTemplateInRegion(
                    screen, template, 0, startY, screen.getWidth(), height,
                    MATCH_THRESHOLD, 5, 4, key
            );

            for (ImageRecognizer.MatchResult match : matches) {
                if (match.confidence >= MATCH_THRESHOLD) {
                    allMatches.add(match);
                    log("找到 '" + key + "' 置信度: " + String.format("%.3f", match.confidence) +
                            " 位置: (" + match.x + ", " + match.y + ")");
                }
            }
        }

        return getOrderedSequenceWithDuplicates(allMatches);
    }

    /**
     * 获取有序的按键序列字符串
     */
    private String getOrderedSequenceWithDuplicates(List<ImageRecognizer.MatchResult> matches) {
        if (matches.isEmpty()) return "";

        // 按X坐标排序（从左到右）
        matches.sort((a, b) -> Integer.compare(a.x, b.x));
        StringBuilder sequence = new StringBuilder();

        for (ImageRecognizer.MatchResult match : matches) {
            sequence.append(match.templateKey);
        }

        return sequence.toString();
    }

    @Override
    public void stop() {
        running = false;
        log("任务被停止");
    }
}
