package com.jiuyin.model;

import com.sun.jna.platform.win32.WinDef;

/**
 * 用来保存选中的窗口
 */
public class SelectedWindow {
    //选中的窗口句柄
    private static WinDef.HWND selectedWindowHandle = null;

    public static void setSelectedWindowHandle(WinDef.HWND hwnd) {
        selectedWindowHandle = hwnd;
    }

    public static WinDef.HWND getSelectedWindowHandle() {
        return selectedWindowHandle;
    }
}
