package com.jiuyin.function.task;

import com.jiuyin.config.AppConfig;
import com.jiuyin.model.SelectedWindow;
import com.jiuyin.nativeapi.CLibrary;
import com.jiuyin.util.ImageDebugUtils;
import com.jiuyin.util.ImageRecognizer;
import com.jiuyin.util.WindowCapture;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.User32;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static com.jiuyin.function.window.WindowHandle.getHwnd;

/**
 * 种地任务
 */
public class FarmTask extends BaseTask {
    private static final double MATCH_THRESHOLD = AppConfig.getMatchThreshold();
    private static final long DETECTION_INTERVAL = AppConfig.getDetectionInterval();

    private final WindowCapture windowCapture = new WindowCapture();
    private final ImageRecognizer imageRecognizer = new ImageRecognizer();
    private BufferedImage template;
    private int screenshotCount = 0;
    private WinDef.HWND gameWindow;
    private final User32 user32 = User32.INSTANCE;

    // 手动校准的偏移量（根据实际偏差调整）
    private int calibrateX = 100;  // 向右调整
    private int calibrateY = -100;  // 向下调整

    public FarmTask(JTextArea logArea) {
        super(logArea);
    }

    @Override
    public String getName() {
        return "种地任务";
    }

    @Override
    public void execute() throws InterruptedException {
        running = true;
        completed = false;

        try {
            log("开始种地任务...");
            ImageDebugUtils.ensureDebugDir();

            // 加载模板图片
            String basePath = System.getProperty("user.dir") + "/src/main/resources/img/farm/山药种子.png";
            template = ImageIO.read(new File(basePath));
            if (template == null) {
                log("模板图片加载失败");
                return;
            }

            // 获取选中的窗口句柄
            gameWindow = SelectedWindow.getSelectedWindowHandle();
            if (gameWindow == null || !windowCapture.isWindowValid(gameWindow)) {
                log("窗口无效或未选择窗口");
                return;
            }

            log("找到窗口: " + windowCapture.getWindowTitle(gameWindow));
            log("校准参数: X=" + calibrateX + ", Y=" + calibrateY);

            int detectionCount = 0;
            while (running && detectionCount < 15) {
                activateGameWindow();

                BufferedImage screen = windowCapture.captureWholeWindow(gameWindow);
                if (screen == null) {
                    Thread.sleep(500);
                    continue;
                }

                log("第 " + (detectionCount + 1) + " 次截图");
                ImageDebugUtils.saveImageToDebugDir(screen, "screenshot");
                screenshotCount++;

                List<ImageRecognizer.MatchResult> matches = imageRecognizer.findTemplateInRegion(
                        screen, template, 0, 0, screen.getWidth(), screen.getHeight(),
                        MATCH_THRESHOLD, 5, 4, "山药种子"
                );

                if (!matches.isEmpty()) {
                    ImageRecognizer.MatchResult match = matches.get(0);
                    log("找到位置: (" + match.x + ", " + match.y + "), 置信度: " + match.confidence);

                    // 直接使用截图坐标 + 手动校准
                    int screenX = match.x + calibrateX;
                    int screenY = match.y + calibrateY;

                    log("校准后坐标: (" + screenX + ", " + screenY + ")");

                    if (performClick(screenX, screenY)) {
                        completed = true;
                        break;
                    }

                } else {
                    log("未找到山药种子");
                }

                Thread.sleep(DETECTION_INTERVAL);
                detectionCount++;
            }

            if (completed) log("种地任务完成");
            else if (running) log("种地任务未完成");
            else log("种地任务被中断");

        } catch (Exception e) {
            log("任务异常: " + e.getMessage());
        } finally {
            running = false;
        }
    }

    /**
     * 执行操作
     */
    private boolean performClick(int screenX, int screenY) {
        try {
            log("移动到: (" + screenX + ", " + screenY + ")");

            activateGameWindow();
            Thread.sleep(200);

            CLibrary.INSTANCE.MoveMouseTo(screenX, screenY);
            Thread.sleep(300);

            return true;

        } catch (Exception e) {
            log("点击失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 激活游戏窗口
     */
    private void activateGameWindow() {
        try {
            if (gameWindow != null) {
                user32.SetForegroundWindow(gameWindow);
                Thread.sleep(200);
            }
        } catch (Exception e) {
            log("激活窗口失败");
        }
    }

    /**
     * 手动校准方法（外部调用调整）
     */
    public void setCalibration(int x, int y) {
        calibrateX = x;
        calibrateY = y;
        log("设置校准参数: X=" + x + ", Y=" + y);
    }

    /**
     * 调试方法：测试不同校准值
     */
    public void testCalibration() {
        log("当前校准: X=" + calibrateX + ", Y=" + calibrateY);
        log("建议调整方向:");
        log("偏左: 增加 calibrateX");
        log("偏右: 减少 calibrateX");
        log("偏上: 增加 calibrateY");
        log("偏下: 减少 calibrateY");
    }
}

