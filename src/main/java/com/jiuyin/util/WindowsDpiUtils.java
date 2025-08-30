package com.jiuyin.util;


import java.awt.*;

public class WindowsDpiUtils {
    /**
     * 获取主显示器的 DPI 缩放比例
     * @return 主显示器的 DPI 缩放比例 (例如 1.0 表示 100%, 1.5 表示 150%)
     */
    public static double getMainDisplayScaling() {
        return getDisplayScaling(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
    }

    /**
     * 获取所有显示器的 DPI 缩放比例数组
     * @return 包含所有显示器 DPI 缩放比例的数组
     */
    public static double[] getAllDisplaysScaling() {
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        double[] scalings = new double[devices.length];
        for (int i = 0; i < devices.length; i++) {
            scalings[i] = getDisplayScaling(devices[i]);
        }
        return scalings;
    }

    /**
     * 获取指定显示设备的 DPI 缩放比例
     * @param device 图形设备
     * @return 指定显示设备的 DPI 缩放比例
     */
    public static double getDisplayScaling(GraphicsDevice device) {
        DisplayMode mode = device.getDisplayMode();
        int screenWidth = mode.getWidth();

        // 获取屏幕的物理尺寸（英寸）
        double screenSizeInch = getScreenSizeInch(device);
        if (screenSizeInch <= 0) {
            // 如果无法获取物理尺寸，使用默认方法
            return getDefaultScaling();
        }

        // 计算物理DPI
        double physicalDPI = screenWidth / screenSizeInch;

        // 计算缩放比例（假设标准DPI为96）
        double scaling = physicalDPI / 96.0;

        // 限制在合理范围内
        return Math.max(0.5, Math.min(4.0, scaling));
    }

    /**
     * 获取屏幕的物理尺寸（对角线英寸）
     * @param device 图形设备
     * @return 屏幕对角线英寸数，如果无法获取返回0
     */
    private static double getScreenSizeInch(GraphicsDevice device) {
        try {
            // 通过反射获取屏幕的物理尺寸
            Class<?> clazz = Class.forName("sun.awt.Win32GraphicsDevice");
            if (clazz.isInstance(device)) {
                java.lang.reflect.Field field = clazz.getDeclaredField("screenWidthMM");
                field.setAccessible(true);
                int widthMM = field.getInt(device);

                field = clazz.getDeclaredField("screenHeightMM");
                field.setAccessible(true);
                int heightMM = field.getInt(device);

                // 计算对角线长度（毫米转换为英寸）
                double diagonalMM = Math.sqrt(widthMM * widthMM + heightMM * heightMM);
                return diagonalMM / 25.4;
            }
        } catch (Exception e) {
            // 反射失败，使用备用方法
        }
        return 0;
    }

    /**
     * 默认的DPI缩放计算方法
     * @return 默认的DPI缩放比例
     */
    private static double getDefaultScaling() {
        // 使用Toolkit获取屏幕分辨率
        int dpi = Toolkit.getDefaultToolkit().getScreenResolution();

        // 计算缩放比例（假设标准DPI为96）
        double scaling = dpi / 96.0;

        // 限制在合理范围内
        return Math.max(0.5, Math.min(4.0, scaling));
    }

}