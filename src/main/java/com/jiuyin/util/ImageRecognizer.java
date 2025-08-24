package com.jiuyin.util;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageRecognizer {

    static {
        // 加载 OpenCV 本地库
        nu.pattern.OpenCV.loadLocally();
    }

    public static class MatchResult {
        public int x;
        public int y;
        public double confidence;
        public String templateKey;

        public MatchResult(int x, int y, double confidence, String templateKey) {
            this.x = x;
            this.y = y;
            this.confidence = confidence;
            this.templateKey = templateKey;
        }
    }

    /**
     * 使用 OpenCV 进行模板匹配
     */
    public List<MatchResult> findTemplateMultipleFast(BufferedImage source, BufferedImage template,
                                                      double threshold, int maxMatches, int step,
                                                      String templateKey) {
        List<MatchResult> results = new ArrayList<>();

        try {
            // 转换 BufferedImage 为 OpenCV Mat
            Mat sourceMat = bufferedImageToMat(source);
            Mat templateMat = bufferedImageToMat(template);

            // 创建结果矩阵
            int resultCols = sourceMat.cols() - templateMat.cols() + 1;
            int resultRows = sourceMat.rows() - templateMat.rows() + 1;
            Mat result = new Mat(resultRows, resultCols, CvType.CV_32FC1);

            // 执行模板匹配
            Imgproc.matchTemplate(sourceMat, templateMat, result, Imgproc.TM_CCOEFF_NORMED);

            // 寻找匹配点
            for (int i = 0; i < maxMatches; i++) {
                Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

                if (mmr.maxVal >= threshold) {
                    Point matchLoc = mmr.maxLoc;
                    results.add(new MatchResult(
                            (int) matchLoc.x,
                            (int) matchLoc.y,
                            mmr.maxVal,
                            templateKey
                    ));

                    // 将已找到的区域置为最小值，避免重复检测
                    Imgproc.rectangle(result,
                            new Point(matchLoc.x - templateMat.width()/2, matchLoc.y - templateMat.height()/2),
                            new Point(matchLoc.x + templateMat.width()/2, matchLoc.y + templateMat.height()/2),
                            new Scalar(0),
                            -1
                    );
                } else {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("OpenCV 匹配错误: " + e.getMessage());
        }

        return results;
    }

    /**
     * 在指定区域进行模板匹配
     */
    public List<MatchResult> findTemplateInRegion(BufferedImage source, BufferedImage template,
                                                  int startX, int startY, int width, int height,
                                                  double threshold, int maxMatches, int step,
                                                  String templateKey) {
        // 裁剪源图像到指定区域
        BufferedImage region = source.getSubimage(startX, startY, width, height);
        List<MatchResult> matches = findTemplateMultipleFast(region, template, threshold, maxMatches, step, templateKey);

        // 调整坐标到全图坐标
        for (MatchResult match : matches) {
            match.x += startX;
            match.y += startY;
        }

        return matches;
    }

    /**
     * 转换 BufferedImage 为 OpenCV Mat
     */
    /**
     * 转换 BufferedImage 为 OpenCV Mat（修复版）
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        try {
            // 确保图像是合适的类型
            BufferedImage convertedImage;
            if (image.getType() == BufferedImage.TYPE_3BYTE_BGR ||
                    image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
                convertedImage = image;
            } else {
                // 转换为 BGR 格式
                convertedImage = new BufferedImage(
                        image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR
                );
                convertedImage.getGraphics().drawImage(image, 0, 0, null);
            }

            byte[] pixels = ((DataBufferByte) convertedImage.getRaster().getDataBuffer()).getData();
            int type = (convertedImage.getType() == BufferedImage.TYPE_BYTE_GRAY) ?
                    CvType.CV_8UC1 : CvType.CV_8UC3;

            Mat mat = new Mat(convertedImage.getHeight(), convertedImage.getWidth(), type);
            mat.put(0, 0, pixels);
            return mat;

        } catch (Exception e) {
            System.out.println("图像转换错误: " + e.getMessage());
            return new Mat();
        }
    }

    /**
     * 调试方法：可视化匹配结果
     */
    public void debugVisualizeMatches(BufferedImage source, BufferedImage template,
                                      List<MatchResult> matches, String outputPath) {
        BufferedImage markedImage = ImageDebugUtils.markMatchPositions(source, matches, template);
        ImageDebugUtils.saveImage(markedImage, outputPath);
    }

    /**
     * 调试方法：测试不同阈值
     */
    public void debugTestThresholds(BufferedImage source, BufferedImage template,
                                    double[] thresholds, String outputDir) {
        for (double threshold : thresholds) {
            List<MatchResult> matches = findTemplateInRegion(
                    source, template, 0, 0, source.getWidth(), source.getHeight(),
                    threshold, 5, 4, "debug"
            );

            if (!matches.isEmpty()) {
                String outputPath = outputDir + "threshold_" + threshold + ".png";
                debugVisualizeMatches(source, template, matches, outputPath);
            }
        }
    }

    /**
     * 调试方法：保存中间处理图像
     */
    public void debugSaveProcessingSteps(BufferedImage source, BufferedImage template, String outputDir) {
        // 保存源图像
        ImageDebugUtils.saveImage(source, outputDir + "source.png");

        // 保存模板图像
        ImageDebugUtils.saveImage(template, outputDir + "template.png");

        // 转换并保存处理后的图像
        try {
            Mat sourceMat = bufferedImageToMat(source);
            Mat templateMat = bufferedImageToMat(template);

            // 这里可以添加更多调试步骤...

        } catch (Exception e) {
            System.out.println("调试步骤保存失败: " + e.getMessage());
        }
    }


}