package com.jiuyin.nativeapi;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface CLibrary extends Library {
    CLibrary INSTANCE = (CLibrary) Native.loadLibrary(
            System.getProperty("user.dir") + "/src/main/resources/dll/igkmlib64.dll",
            CLibrary.class
    );

    // 声明将要调用的DLL中的方法,可以是多个方法(此处示例调用本地动态库msvcrt.dll中的printf()方法)
    void printf(String format, Object... args);
    String GetModel();
    String GetSerialNumber();
    String GetProductionDate();
    String GetFirmwareVersion();
    String GetDeviceListByModel(String Model);
    int SelectDeviceBySerialNumber(String SerialNumber);
    int SelectDeviceByVIDPID(int VID,int PID);
    int IsDeviceConnected();
    int PressKeyByName(String KeyName);
    int PressKeyByValue(int KeyValue);
    //int PressAndReleaseKey(int Key);
    int ReleaseKeyByName(String KeyName);
    int ReleaseKeyByValue(int KeyValue);
    int PressAndReleaseKeyByName(String KeyName);
    int PressAndReleaseKeyByValue(int KeyValue);
    int InputString(String Str);
    int ReleaseAllKey();
    int GetCapsLock();
    int GetNumLock();
    int SetPressKeyDelay(int MinDelay,int MaxDelay);
    int SetInputStringIntervalTime(int MinDelay,int MaxDelay);
    int SetCaseSensitive(int Discriminate);
    int PressMouseButton(int mButton);
    int ReleaseMouseButton(int mButton);
    int PressAndReleaseMouseButton(int mButton);
    int IsMouseButtonPressed(int mButton);
    int ReleaseAllMouseButton();
    int MoveMouseTo(int X,int Y);
    int MoveMouseRelative(int X,int Y);
    int MoveMouseWheel(int Z);
    int GetMousePosition();
    int SetMousePosition(int X,int Y);
    int SetPressMouseButtonDelay(int MinDelay,int MaxDelay);
    int SetMouseMovementDelay(int MinDelay,int MaxDelay);
    int SetMouseMovementSpeed(int SpeedValue);
}
