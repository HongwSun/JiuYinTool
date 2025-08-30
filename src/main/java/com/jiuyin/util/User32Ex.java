package com.jiuyin.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * 扩展的User32接口
 */
public interface User32Ex extends StdCallLibrary {
    User32Ex INSTANCE = Native.load("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);
    boolean ClientToScreen(WinDef.HWND hWnd, WinDef.POINT point);
    boolean GetWindowRect(WinDef.HWND hWnd, WinDef.RECT lpRect);
    boolean GetClientRect(WinDef.HWND hWnd, WinDef.RECT lpRect);
    int GetSystemMetrics(int nIndex);
    int GetDpiForWindow(WinDef.HWND hwnd);
    int GetDpiForSystem(WinDef.HWND hwnd);
    int GetDpiForSystem();
    WinDef.HDC GetWindowDC(WinDef.HWND hwnd);
}