package com.jiuyin.function.window;


import com.jiuyin.util.User32Ex;
import com.jiuyin.util.WindowsDpiUtils;

/**
 * 根据系统dpi坐标转换
 */
public class CoordinateConverter {

    private static final User32Ex user32 = User32Ex.INSTANCE;

    /**
     * 客户端坐标转屏幕坐标
     */
    public static int[] clientToScreen( int clientX, int clientY) {
        //获取缩放
        double dpi = WindowsDpiUtils.getMainDisplayScaling();

        int x  = (int)(clientX/dpi);
        int y  =(int)(clientY/dpi);

        return new int[]{x, y};
    }
}