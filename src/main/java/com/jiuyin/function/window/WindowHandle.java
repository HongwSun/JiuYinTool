package com.jiuyin.function.window;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.platform.win32.User32;
import java.util.ArrayList;
import java.util.List;

public class WindowHandle{

    /**
     * 根据进程名称获取 PID 列表
     */
    public static List<Integer> getPidsByProcessName(String processName) {
        List<Integer> pids = new ArrayList<>();

        // 创建系统快照
        WinNT.HANDLE hSnap = Kernel32.INSTANCE.CreateToolhelp32Snapshot(
                Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));

        if (hSnap == null || WinBase.INVALID_HANDLE_VALUE.equals(hSnap)) {
            return pids;
        }

        try {
            Tlhelp32.PROCESSENTRY32.ByReference entry = new Tlhelp32.PROCESSENTRY32.ByReference();
            if (Kernel32.INSTANCE.Process32First(hSnap, entry)) {
                do {
                    String exeFile = Native.toString(entry.szExeFile);
                    if (exeFile.equalsIgnoreCase(processName)) {
                        pids.add(entry.th32ProcessID.intValue());
                    }
                } while (Kernel32.INSTANCE.Process32Next(hSnap, entry));
            }
        } finally {
            Kernel32.INSTANCE.CloseHandle(hSnap);
        }
        return pids;
    }

    /**
     *根据 PID 获取该进程的主窗口句柄
     */
    public static WinDef.HWND getMainWindowHandleByPid(int pid) {
        // 使用回调方式枚举窗口
        final WinDef.HWND[] result = new WinDef.HWND[1];
        User32.INSTANCE.EnumWindows((hwnd, data) -> {
            IntByReference pidRef = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hwnd, pidRef);
            if (pidRef.getValue() == pid) {
                // 仅取可见的顶层窗口
                if (User32.INSTANCE.IsWindowVisible(hwnd)) {
                    result[0] = hwnd;
                    return false;   // 找到后停止枚举
                }
            }
            return true;
        }, null);
        return result[0];
    }

    /**
     * 获取窗口句柄(获取第一个窗口)
     * @return
     */
    public static  WinDef.HWND getHwnd(){
        String procName = "fxgame.exe";
        List<Integer> pids = getPidsByProcessName(procName);
        System.out.println("进程 " + procName + " 的 PID 列表: " + pids);

        if (!pids.isEmpty()) {
            int pid = pids.get(0);
            WinDef.HWND hwnd = getMainWindowHandleByPid(pid);
            return hwnd;
        }else{
            return null;
        }
    }

    /**
     * 激活游戏窗口
     */
    private void activateGameWindow(WinDef.HWND gameWindow) {
        try {
            if (gameWindow != null) {
                User32.INSTANCE.SetForegroundWindow(gameWindow);
                Thread.sleep(200);
            }
        } catch (Exception e) {

        }
    }

}
