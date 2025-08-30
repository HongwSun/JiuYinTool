package com.jiuyin.config;

import java.util.HashMap;
import java.util.Map;

/**
 * 按键识别配置类
 */
public class KeyConfig {
    // 按键模板图片路径映射
    public static final Map<String, String> KEY_TEMPLATES = new HashMap<>();

    static {
        String basePath =  "src/main/resources/img/tuanlian/";
        KEY_TEMPLATES.put("W", basePath + "pic_dance_up_grey.png");
        KEY_TEMPLATES.put("S", basePath + "pic_dance_down_grey.png");
        KEY_TEMPLATES.put("A", basePath + "pic_dance_left_grey.png");
        KEY_TEMPLATES.put("D", basePath + "pic_dance_right_grey.png");
        KEY_TEMPLATES.put("J", basePath + "pic_dance_J.png");
        KEY_TEMPLATES.put("K", basePath + "pic_dance_K.png");
    }
}