package test;


import com.jiuyin.function.opencv.ImageRecognizer;
import com.jiuyin.function.window.WindowCapture;
import com.jiuyin.function.window.WindowHandle;
import com.jiuyin.model.SelectedWindow;
import com.jiuyin.nativeapi.CLibrary;
import com.jiuyin.util.ImageDebugUtils;
import com.sun.jna.platform.win32.WinDef;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Test {

    public static void main(String[] args) throws InterruptedException, IOException {


	/*
    //获取设备型号
    System.out.println("获取设备型号:"+CLibrary.INSTANCE.GetModel());
    //获取设备序列号
    System.out.println("获取设备序列号:"+CLibrary.INSTANCE.GetSerialNumber());
    //获取设备生产日期
    System.out.println("获取设备生产日期:"+CLibrary.INSTANCE.GetProductionDate());
    //获取设备版本号
    System.out.println("获取设备版本号:"+CLibrary.INSTANCE.GetFirmwareVersion());
    //获取设备列表
    System.out.println("获取设备列表:"+CLibrary.INSTANCE.GetDeviceListByModel(""));
    //选择设备 根据设备序列号选择设备
    //System.out.println(CLibrary.INSTANCE.SelectDeviceBySerialNumber("CAEFA36912D69136"));
    //根据设备VIDPID选择设备
    //System.out.println(CLibrary.INSTANCE.SelectDeviceByVIDPID(1234,5678));
    //设备是否连接
    System.out.println("设备是否连接:"+CLibrary.INSTANCE.IsDeviceConnected());
    //System.out.println(CLibrary.INSTANCE.PressKeyByName("win"));
    //System.out.println(CLibrary.INSTANCE.PressKeyByValue(91));
    //System.out.println(CLibrary.INSTANCE.ReleaseKeyByName("win"));
    //System.out.println(CLibrary.INSTANCE.ReleaseKeyByValue(91));
    try {
		Thread.currentThread();
		Thread.sleep(500);//延时500毫秒
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   // System.out.println("按一次win键 键名:"+CLibrary.INSTANCE.PressAndReleaseKeyByName("win"));
    try {
		Thread.currentThread();
		Thread.sleep(100);//延时500毫秒
	} catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}

    //释放所有键盘按键
    System.out.println("释放所有键盘按键:"+CLibrary.INSTANCE.ReleaseAllKey());
    //System.out.println(CLibrary.INSTANCE.PressAndReleaseKeyByValue(91));
    //模拟人工输入字符串
    //System.out.println(CLibrary.INSTANCE.InputString("winq中间ASDQWER"));
    // 获取CapsLock（大写锁定）状态
    System.out.println("获取CapsLock（大写锁定）状态:"+CLibrary.INSTANCE.GetCapsLock());
    //获取NumLock（数字键盘锁定）状态
    System.out.println("获取NumLock（数字键盘锁定）状态:"+CLibrary.INSTANCE.GetNumLock());
    //设置按键延时
    System.out.println("设置按键延时:"+CLibrary.INSTANCE.SetPressKeyDelay(50,60));
    //设置输入字符串间隔时间
    System.out.println("设置输入字符串间隔时间:"+CLibrary.INSTANCE.SetInputStringIntervalTime(50,100));
    //设置是否区分大小写
    System.out.println("设置是否区分大小写:"+CLibrary.INSTANCE.SetCaseSensitive(0));

    //按下鼠标键
    //System.out.println("按下鼠标键:"+CLibrary.INSTANCE.PressMouseButton(3));

    try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    //释放鼠标键
    //System.out.println("释放鼠标键:"+CLibrary.INSTANCE.ReleaseMouseButton(3));
    //按下并释放鼠标键
    //System.out.println("按下并释放鼠标键:"+CLibrary.INSTANCE.PressAndReleaseMouseButton(3));
    //鼠标键是否按下
    System.out.println("鼠标键是否按下:"+CLibrary.INSTANCE.IsMouseButtonPressed(3));
    //释放所有鼠标按键
    System.out.println("释放所有鼠标按键:"+CLibrary.INSTANCE.ReleaseAllMouseButton());
    //移动鼠标到指定坐标
    //System.out.println("移动鼠标到指定坐标:"+CLibrary.INSTANCE.MoveMouseTo(10,10));
    try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	*/

		 /*
    //相对移动鼠标
    System.out.println("相对移动鼠标:"+CLibrary.INSTANCE.MoveMouseRelative(20,20));
    try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    //移动鼠标滚轮
    System.out.println("移动鼠标滚轮:"+CLibrary.INSTANCE.MoveMouseWheel(2));


    //设置鼠标按键延时
    System.out.println("设置鼠标按键延时:"+CLibrary.INSTANCE.SetPressMouseButtonDelay(50,50));
    //设置鼠标移动延时
    System.out.println("设置鼠标移动延时:"+CLibrary.INSTANCE.SetMouseMovementDelay(8,8));
    //设置鼠标移动速度
    System.out.println("设置鼠标移动速度:"+CLibrary.INSTANCE.SetMouseMovementSpeed(7));
    //设置鼠标位置
    System.out.println("设置鼠标位置:"+CLibrary.INSTANCE.SetMousePosition(50,50));
    */
//        try {
//            Thread.sleep(1000);//延时1
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        CLibrary.INSTANCE.SetMouseMovementDelay(8,8);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        CLibrary.INSTANCE.MoveMouseTo(100,100);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        //获取鼠标位置
//        int a = CLibrary.INSTANCE.GetMousePosition();
//        int x,y,XY;
//        XY =a;// CLibrary.INSTANCE.GetMousePosition();
//        x = (XY >> 16) & 0xFFFF;
//        y = XY & 0xFFFF;
//        System.out.println("获取鼠标位置:"+x+","+y);
//
//
//
//
    }


}
