package com.day.getBazzar;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.setting.Setting;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class GlobalVar {
    static  String USER;
    static  String PASSWORD;
    static int TotalTimes;
    static String LOCAL_JSON_PATH;
    static  String DB_URL;

    /**
     * 用于从目录中加载配置文件
     * @throws IOException
     */
    public  void readConfig() throws IOException {
        Setting cfg = new Setting("E:\\modding\\SmallProject\\GetBazzar\\config\\bazzar.setting", CharsetUtil.CHARSET_UTF_8, true);
        //全局参数读入
        USER = cfg.getStr("user","mysql","root");
        PASSWORD = cfg.getByGroup("pass","mysql");
        DB_URL = cfg.getByGroup("url","mysql");
        TotalTimes = cfg.getInt("TotalTimes","API",2);
        LOCAL_JSON_PATH = cfg.getByGroup("path","API");
    }
    // 本地JSON用于测试
    //public static JSONObject SB_BAZZAR_JSON_FULL = toolClass.LoadLocalJSON(JSON_PATH);

    static  URL SB_BAZZAR_API;
    static {
        try {
             SB_BAZZAR_API = new URL("https://api.hypixel.net/skyblock/bazaar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
