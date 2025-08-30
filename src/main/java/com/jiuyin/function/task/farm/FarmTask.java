package com.jiuyin.function.task.farm;

import com.jiuyin.config.AppConfig;
import com.jiuyin.function.task.basetask.BaseTask;
import com.jiuyin.function.window.WindowHandle;
import com.jiuyin.model.SelectedWindow;
import com.jiuyin.nativeapi.CLibrary;
import com.jiuyin.function.window.CoordinateConverter;
import com.jiuyin.util.ImageDebugUtils;
import com.jiuyin.function.opencv.ImageRecognizer;
import com.jiuyin.function.window.WindowCapture;
import com.sun.jna.platform.win32.WinDef;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 种地任务
 */
public class FarmTask extends BaseTask {
    private static final double MATCH_THRESHOLD = AppConfig.getMatchThreshold();
    private static final long DETECTION_INTERVAL = AppConfig.getDetectionInterval();

    private final WindowCapture windowCapture = new WindowCapture();
    private final ImageRecognizer imageRecognizer = new ImageRecognizer();
    private WinDef.HWND gameWindow;

    private final String seedImgPath = "src/main/resources/img/farm/种子.png";
    private final String fertilizerImgPath = "src/main/resources/img/farm/肥料.png";
    private final String onGingImgPath = "src/main/resources/img/farm/拾取中.png";
    private final String harvestImgPath = "src/main/resources/img/farm/全部拾取.png";

    private BufferedImage seedImg;
    private BufferedImage fertilizerImg;
    private BufferedImage onGingImg;
    private BufferedImage harvestImg;


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
            // 获取选中的窗口句柄
            gameWindow = SelectedWindow.getSelectedWindowHandle();
            if (gameWindow == null || !windowCapture.isWindowValid(gameWindow)) {
                log("窗口无效或未选择窗口");
                return;
            }
            log("开始种地任务...");
            //激活窗口
            WindowHandle.activateGameWindow(gameWindow);

            //初始化图片
            initializeImages();

            while(running){

                //找种子并点击
                findAndClickImage(seedImg, "种子", 15, 3, true);

                //找到作物土地快并点击
                findAndClickFarmLand();

                Thread.sleep(200);

                //找到肥料并点击
                for(int m =1;m<=6;m++){
                    findAndClickImage(fertilizerImg, "肥料", 15, 3, true);
                }

                findAndClickFarmLand();

                Thread.sleep(200);

                while(!isDone()){
                    continue;
                }

                Thread.sleep(200);

                //全部拾取
                 findAndClickImage(harvestImg, "全部拾取", 15, 1, false);
                Thread.sleep(200);

            }
        } catch (Exception e) {
            log("任务异常: " + e.getMessage());
        }
    }

    /**
     * 通用的查找并点击图片的方法
     * @param targetImage 目标图片
     * @param imageName 图片名称（用于日志）
     * @param maxAttempts 最大尝试次数
     * @param mouseButton 鼠标按钮（1左键，3右键）
     * @param needWaitForCompletion 是否需要等待操作完成（如种植/施肥完成）
     * @return 是否成功找到并点击
     */
    private boolean findAndClickImage(BufferedImage targetImage, String imageName,
                                      int maxAttempts, int mouseButton,
                                      boolean needWaitForCompletion) throws InterruptedException {
        int detectionCount = 0;
        while (running && detectionCount < maxAttempts) {
            BufferedImage screen = windowCapture.captureWholeWindow(gameWindow);

            if (screen == null) {
                Thread.sleep(500);
                continue;
            }

            List<ImageRecognizer.MatchResult> matches = imageRecognizer.findTemplateInRegion(
                    screen, targetImage, 0, 0, screen.getWidth(), screen.getHeight(),
                    MATCH_THRESHOLD, 5, 4, imageName
            );

            if (!matches.isEmpty()) {
                ImageRecognizer.MatchResult match = matches.get(0);
                log("找到" + imageName + "位置: (" + match.x + ", " + match.y + "), 置信度: " + match.confidence);

                try {
                    int[] screenPoint = CoordinateConverter.clientToScreen(match.x, match.y);
                    CLibrary.INSTANCE.MoveMouseTo(0, 0);
                    CLibrary.INSTANCE.MoveMouseTo(screenPoint[0], screenPoint[1]);

                    CLibrary.INSTANCE.PressMouseButton(mouseButton);
                    Thread.sleep(20);
                    CLibrary.INSTANCE.ReleaseMouseButton(mouseButton);
                    Thread.sleep(200); // 延时确保操作生效

                    if (needWaitForCompletion) {
                        while (!isDone()) {
                            continue;
                        }
                    }
                    return true;
                } catch (Exception e) {
                    log(imageName + "点击失败: " + e.getMessage());
                    return false;
                }
            } else {
                log("未找到" + imageName);
            }
            Thread.sleep(DETECTION_INTERVAL);
            detectionCount++;
        }
        return false;
    }


    /**
     * 找到土地快并点击
     */
    private void findAndClickFarmLand() throws InterruptedException {
        BufferedImage screen = windowCapture.captureWholeWindow(gameWindow);

        int[] screenPoint = CoordinateConverter.clientToScreen((int)(screen.getWidth()/2),(int)(screen.getHeight()/2));
        CLibrary.INSTANCE.MoveMouseTo(0,0);
        CLibrary.INSTANCE.MoveMouseTo(screenPoint[0],screenPoint[1]);
        CLibrary.INSTANCE.PressMouseButton(1);
        Thread.sleep(20);
        CLibrary.INSTANCE.ReleaseMouseButton(1);

    }

    /**
     * 检测是不是操作结束 种植&收获
     */
    private boolean isDone() throws InterruptedException, IOException {

        BufferedImage ifOnGoing = windowCapture.captureWholeWindow(gameWindow);

        if (ifOnGoing == null) {
            Thread.sleep(500);
        }

        List<ImageRecognizer.MatchResult> ifOnGoingMatches = imageRecognizer.findTemplateInRegion(
                ifOnGoing, onGingImg, 0, 0, ifOnGoing.getWidth(), ifOnGoing.getHeight(),
                MATCH_THRESHOLD, 1, 4, "拾取中"
        );

        BufferedImage imgDebug =  ImageDebugUtils.markMatchPositions(ifOnGoing,
                ifOnGoingMatches,
                onGingImg);
//        ImageDebugUtils.saveImageToDebugDir(imgDebug,"going");
        //如果检测到了，就说明在拾取
        if(!ifOnGoingMatches.isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    /**
     * 初始化图片资源
     */
    private void initializeImages() {
        try {
            seedImg = ImageIO.read(new File(seedImgPath));
            log("种子图片加载完毕");
        } catch (IOException e) {
            log("种子图片加载失败: " + e.getMessage());
        }

        try {
            fertilizerImg = ImageIO.read(new File(fertilizerImgPath));
            log("肥料图片加载完毕");
        } catch (IOException e) {
            log("肥料图片加载失败: " + e.getMessage());
        }

        try {
            onGingImg = ImageIO.read(new File(onGingImgPath));
            log("拾取过程图片加载完毕");
        } catch (IOException e) {
            log("拾取过程图片加载失败: " + e.getMessage());
        }

        try {
            harvestImg = ImageIO.read(new File(harvestImgPath));
            log("全部拾取图片加载完毕");
        } catch (IOException e) {
            log("全部拾取图片加载失败: " + e.getMessage());
        }
    }
}

