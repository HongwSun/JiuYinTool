package com.jiuyin.util;

import com.jiuyin.function.opencv.ImageRecognizer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 图像调试工具类
 */
public class ImageDebugUtils {
    private static final String DEFAULT_DEBUG_DIR = "src/main/resources/debug_screenshots/";

    /**
     * 保存图像到文件
     */
    public static boolean saveImage(BufferedImage image, String filePath) {
        try {
            // 确保目录存在
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            String format = filePath.toLowerCase().endsWith(".png") ? "PNG" : "JPG";
            boolean success = ImageIO.write(image, format, file);
            if (success) {
                System.out.println("图像已保存: " + filePath);
            } else {
                System.out.println("保存图像失败，不支持的格式: " + filePath);
            }
            return success;
        } catch (IOException e) {
            System.out.println("保存图像错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 保存图像到调试目录（带时间戳）
     */
    public static String saveImageToDebugDir(BufferedImage image, String prefix) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        String filename = DEFAULT_DEBUG_DIR + prefix + "_" + timestamp + ".png";
        saveImage(image, filename);
        return filename;
    }

    /**
     * 在图像上标记匹配位置
     */
    public static BufferedImage markMatchPositions(BufferedImage originalImage,
                                                   List<ImageRecognizer.MatchResult> matches,
                                                   BufferedImage template) {
        return markMatchPositions(originalImage, matches, template, Color.RED, 2);
    }

    /**
     * 在图像上标记匹配位置（自定义颜色和线宽）
     */
    public static BufferedImage markMatchPositions(BufferedImage originalImage,
                                                   List<ImageRecognizer.MatchResult> matches,
                                                   BufferedImage template,
                                                   Color color, int strokeWidth) {
        // 创建副本用于标记
        BufferedImage markedImage = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR
        );

        Graphics2D g2d = markedImage.createGraphics();
        try {
            // 绘制原图
            g2d.drawImage(originalImage, 0, 0, null);

            // 设置绘制样式
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(strokeWidth));
            g2d.setFont(new Font("Arial", Font.BOLD, 12));

            // 标记每个匹配位置
            for (int i = 0; i < matches.size(); i++) {
                ImageRecognizer.MatchResult match = matches.get(i);

                // 画矩形框
                g2d.drawRect(match.x, match.y, template.getWidth(), template.getHeight());

                // 画十字线
                int centerX = match.x + template.getWidth() / 2;
                int centerY = match.y + template.getHeight() / 2;
                g2d.drawLine(centerX - 10, centerY, centerX + 10, centerY);
                g2d.drawLine(centerX, centerY - 10, centerX, centerY + 10);

                // 写置信度和序号
                String label = String.format("#%d: %.3f", i + 1, match.confidence);
                g2d.drawString(label, match.x, match.y - 5);
            }

        } finally {
            g2d.dispose();
        }

        return markedImage;
    }

    /**
     * 创建调试目录
     */
    public static void ensureDebugDir() {
        File debugDir = new File(DEFAULT_DEBUG_DIR);
        if (!debugDir.exists()) {
            debugDir.mkdirs();
        }
    }

    /**
     * 获取调试目录路径
     */
    public static String getDebugDir() {
        return DEFAULT_DEBUG_DIR;
    }

    /**
     * 清空调试目录
     */
    public static void clearDebugDir() {
        File debugDir = new File(DEFAULT_DEBUG_DIR);
        if (debugDir.exists() && debugDir.isDirectory()) {
            File[] files = debugDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }

    /**
     * 比较两个图像的差异（可视化）
     */
    public static BufferedImage visualizeDifference(BufferedImage image1, BufferedImage image2) {
        int width = Math.min(image1.getWidth(), image2.getWidth());
        int height = Math.min(image1.getHeight(), image2.getHeight());

        BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = image1.getRGB(x, y);
                int rgb2 = image2.getRGB(x, y);

                if (rgb1 != rgb2) {
                    // 差异处显示为红色
                    diffImage.setRGB(x, y, Color.RED.getRGB());
                } else {
                    // 相同处显示为原图
                    diffImage.setRGB(x, y, rgb1);
                }
            }
        }

        return diffImage;
    }

    /**
     * 调整图像大小
     */
    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = resizedImage.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g2d.dispose();
        }
        return resizedImage;
    }

    /**
     * 转换图像颜色模式
     */
    public static BufferedImage convertColorMode(BufferedImage image, int imageType) {
        BufferedImage convertedImage = new BufferedImage(
                image.getWidth(), image.getHeight(), imageType
        );
        Graphics2D g2d = convertedImage.createGraphics();
        try {
            g2d.drawImage(image, 0, 0, null);
        } finally {
            g2d.dispose();
        }
        return convertedImage;
    }
}