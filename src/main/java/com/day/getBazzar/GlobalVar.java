package com.day.getBazzar;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.setting.Setting;

import java.io.IOException;

public class GlobalVar {
    static String USER;
    static String PASSWORD;
    static String LOCAL_JSON_PATH;
    static String DB_URL;
    public static String DB_NM;
    public static String DB_DAY;
    public static String DB_WK;
    public static String DB_MO;
    static int TotalTimes;
    static int API_Time;
    static int reConnectTime;
    static int statistics_day;
    static int statistics_week;
    static int statistics_month;

    /**
     * 用于从目录中加载配置文件
     * @throws IOException
     */
    public  void readConfig()  {
        Setting cfg = new Setting("\\config\\bazzar.setting", CharsetUtil.CHARSET_UTF_8, true);
        //全局参数读入
        USER = cfg.getStr("user","mysql","root");
        PASSWORD = cfg.getByGroup("pass","mysql");
        DB_URL = cfg.getByGroup("url","mysql");
        DB_NM = cfg.getByGroup("DB_nm","mysql");
        DB_DAY = cfg.getByGroup("DB_day","mysql");
        DB_WK = cfg.getByGroup("DB_week","mysql");
        DB_MO = cfg.getByGroup("DB_month","mysql");
        TotalTimes = cfg.getInt("TotalTimes","API",2);
        LOCAL_JSON_PATH = cfg.getByGroup("path","API");
        API_Time = cfg.getInt("API_Time","API",60);
        reConnectTime = cfg.getInt("reConnectTime","API",5);
        statistics_day = cfg.getInt("statistics_day","Bazzar",1440);
        statistics_week = cfg.getInt("statistics_week","Bazzar",7);
        statistics_month = cfg.getInt("statistics_month","Bazzar",30);
    }
    // 本地JSON用于测试
    //public static JSONObject SB_BAZZAR_JSON_FULL = toolClass.LoadLocalJSON(JSON_PATH);

    static String SB_BAZZAR_API = "https://api.hypixel.net/skyblock/bazaar";

}
