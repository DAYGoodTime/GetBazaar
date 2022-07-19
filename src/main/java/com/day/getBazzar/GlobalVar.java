package com.day.getBazzar;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.db.ds.DSFactory;
import cn.hutool.setting.Setting;

import javax.sql.DataSource;

public class GlobalVar {
    public static String SB_BAZZAR_API = "https://api.hypixel.net/skyblock/bazaar";
    public static String TB_NM;
    public static String TB_DAY;
    public static String TB_WK;
    public static String TB_MO;
    public static String DB_NAME;
    public static DataSource bazzar_ds;
    static int TotalTimes;
    static int API_Time;
    static int reConnectTime;
    static int statistics_day;
    static int statistics_week;
    static int statistics_month;
    /**
     * 用于从目录中加载配置文件
     */
    public  void readConfig()  {
        Setting cfg = new Setting("\\config\\bazzar.setting", CharsetUtil.CHARSET_UTF_8, true);
        bazzar_ds = DSFactory.create(cfg).getDataSource("mysql");
        //全局参数读入
        DB_NAME = cfg.getByGroup("DB_name","mysql");
        TB_NM = cfg.getByGroup("TB_nm","DataBase");
        TB_DAY = cfg.getByGroup("TB_day","DataBase");
        TB_WK = cfg.getByGroup("TB_week","DataBase");
        TB_MO = cfg.getByGroup("TB_month","DataBase");
        TotalTimes = cfg.getInt("TotalTimes","API",2);
        API_Time = cfg.getInt("API_Time","API",60);
        reConnectTime = cfg.getInt("reConnectTime","API",5);
        statistics_day = cfg.getInt("statistics_day","API",1440);
        statistics_week = cfg.getInt("statistics_week","API",7);
        statistics_month = cfg.getInt("statistics_month","API",30);
    }


}
