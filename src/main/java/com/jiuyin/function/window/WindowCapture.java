package com.jiuyin.function.window;

import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.Memory;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;


/**
 * 根据窗口句柄进行屏幕截图
 */
public class WindowCapture {
    private static final User32 user32 = User32.INSTANCE;
    private static final GDI32 gdi32 = GDI32.INSTANCE;
    private static final int SRCCOPY = 0x00CC0020;

    /**
     * 检查窗口是否有效且可见
     * @param hwnd 窗口句柄
     * @return 是否有效
     */
    public boolean isWindowValid(WinDef.HWND hwnd) {
        if (hwnd == null) {
            return false;
        }
        return user32.IsWindow(hwnd) && user32.IsWindowVisible(hwnd);
    }

    /**
     * 获取窗口客户区截图
     * @param hwnd 窗口句柄
     * @return 截图图像，失败返回null
     */
    public BufferedImage captureClientArea(WinDef.HWND hwnd) {
        if (!isWindowValid(hwnd)) {
            System.err.println("窗口无效或不可见");
            return null;
        }

        // 获取客户区大小
        WinDef.RECT rect = new WinDef.RECT();
        if (!user32.GetClientRect(hwnd, rect)) {
            System.err.println("获取客户区大小失败");
            return null;
        }

        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        if (width <= 0 || height <= 0) {
            System.err.println("窗口尺寸无效: " + width + "x" + height);
            return null;
        }

        return captureArea(hwnd, 0, 0, width, height, true);
    }

    /**
     * 获取整个窗口截图（包括标题栏和边框）
     * @param hwnd 窗口句柄
     * @return 截图图像，失败返回null
     */
    public BufferedImage captureWholeWindow(WinDef.HWND hwnd) {
        if (!isWindowValid(hwnd)) {
            System.err.println("窗口无效或不可见");
            return null;
        }

        // 获取窗口位置和大小
        WinDef.RECT rect = new WinDef.RECT();
        if (!user32.GetWindowRect(hwnd, rect)) {
            System.err.println("获取窗口位置失败");
            return null;
        }

        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        if (width <= 0 || height <= 0) {
            System.err.println("窗口尺寸无效: " + width + "x" + height);
            return null;
        }

        return captureArea(hwnd, rect.left, rect.top, width, height, false);
    }

    /**
     * 截取指定区域
     * @param hwnd 窗口句柄
     * @param x 区域左上角X坐标
     * @param y 区域左上角Y坐标
     * @param width 区域宽度
     * @param height 区域高度
     * @param isClientArea 是否是客户区坐标
     * @return 截图图像
     */
    private BufferedImage captureArea(WinDef.HWND hwnd, int x, int y, int width, int height, boolean isClientArea) {
        WinDef.HDC hdcWindow = null;
        WinDef.HDC hdcMemDC = null;
        WinDef.HBITMAP hBitmap = null;
        WinNT.HANDLE oldObj = null; // 改为 WinNT.HANDLE 类型

        try {
            // 获取窗口设备上下文
            if (isClientArea) {
                hdcWindow = user32.GetDC(hwnd);
            } else {
                // 对于整个窗口截图，使用桌面设备上下文
                hdcWindow = user32.GetDC(null);
            }

            if (hdcWindow == null) {
                System.err.println("获取设备上下文失败");
                return null;
            }

            // 创建内存设备上下文
            hdcMemDC = gdi32.CreateCompatibleDC(hdcWindow);
            if (hdcMemDC == null) {
                System.err.println("创建内存设备上下文失败");
                return null;
            }

            // 创建兼容位图
            hBitmap = gdi32.CreateCompatibleBitmap(hdcWindow, width, height);
            if (hBitmap == null) {
                System.err.println("创建位图失败");
                return null;
            }

            // 选择位图到内存设备上下文 - 返回类型是 WinNT.HANDLE
            oldObj = gdi32.SelectObject(hdcMemDC, hBitmap);
            if (oldObj == null) {
                System.err.println("选择位图到设备上下文失败");
                return null;
            }

            // 复制图像到内存设备上下文
            boolean success;
            if (isClientArea) {
                success = gdi32.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, 0, 0, SRCCOPY);
            } else {
                // 对于整个窗口，需要调整坐标
                success = gdi32.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, x, y, SRCCOPY);
            }

            if (!success) {
                System.err.println("图像复制失败");
                return null;
            }

            // 获取位图数据
            return getBitmapData(hdcWindow, hBitmap, width, height);

        } catch (Exception e) {
            System.err.println("截图过程中发生异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            // 清理资源
            if (oldObj != null && hdcMemDC != null) {
                gdi32.SelectObject(hdcMemDC, oldObj);
            }
            if (hBitmap != null) {
                gdi32.DeleteObject(hBitmap);
            }
            if (hdcMemDC != null) {
                gdi32.DeleteDC(hdcMemDC);
            }
            if (hdcWindow != null) {
                if (isClientArea) {
                    user32.ReleaseDC(hwnd, hdcWindow);
                } else {
                    user32.ReleaseDC(null, hdcWindow);
                }
            }
        }
    }

    /**
     * 从位图获取图像数据
     */
    private BufferedImage getBitmapData(WinDef.HDC hdc, WinDef.HBITMAP hBitmap, int width, int height) {
        // 创建BITMAPINFO结构
        WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
        bmi.bmiHeader.biSize = 40; // BITMAPINFOHEADER大小
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height; // 负值表示自上而下的DIB
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32; // 32位ARGB
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;
        bmi.bmiHeader.biSizeImage = width * height * 4;

        // 分配内存来存储像素数据
        int bufferSize = width * height * 4;
        Memory buffer = new Memory(bufferSize);

        // 获取位图数据
        int result = gdi32.GetDIBits(hdc, hBitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);
        if (result == 0) {
            System.err.println("获取位图数据失败");
            return null;
        }

        // 创建BufferedImage并填充数据
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        byte[] byteData = buffer.getByteArray(0, bufferSize);

        // 转换BGRA到ARGB
        for (int i = 0, j = 0; i < pixels.length; i++, j += 4) {
            int b = byteData[j] & 0xFF;
            int g = byteData[j + 1] & 0xFF;
            int r = byteData[j + 2] & 0xFF;
            int a = byteData[j + 3] & 0xFF;
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        return image;
    }

    /**
     * 根据窗口标题查找窗口句柄
     * @param windowTitle 窗口标题（支持部分匹配）
     * @return 窗口句柄，未找到返回null
     */
    public WinDef.HWND findWindowByTitle(String windowTitle) {
        return user32.FindWindow(null, windowTitle);
    }

    /**
     * 根据窗口类名查找窗口句柄
     * @param className 窗口类名
     * @return 窗口句柄，未找到返回null
     */
    public WinDef.HWND findWindowByClass(String className) {
        return user32.FindWindow(className, null);
    }

    /**
     * 获取窗口标题
     * @param hwnd 窗口句柄
     * @return 窗口标题
     */
    public String getWindowTitle(WinDef.HWND hwnd) {
        if (hwnd == null) {
            return null;
        }

        char[] buffer = new char[1024];
        int length = user32.GetWindowText(hwnd, buffer, buffer.length);
        if (length > 0) {
            return new String(buffer, 0, length);
        }
        return null;
    }
}
