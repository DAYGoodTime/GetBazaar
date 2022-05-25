package com.day.getBazzar;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class GlobalVar {
    static  String DB_PORT;
    static  String DB_HOST;
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
        // 使用字节流方式读取文件，这里使用的是绝对路径读取配置文件
        String filePath = "E:\\modding\\SmallProject\\GetBazzar\\bazzar.properties";
        InputStream in = new BufferedInputStream(new FileInputStream(filePath));
        Properties cfg = new Properties();
        // 将配置文件以流的形式读取到Properties对象中
        cfg.load(in);
        // 从Properties对象的使用方法和Map一样，不过其get方法返回的是Object对象
        //全局参数读入
        DB_HOST = cfg.get("SQL.host").toString();
        DB_PORT = cfg.get("SQL.PORT").toString();
        USER = cfg.get("SQL.User").toString();
        PASSWORD = cfg.get("SQL.PW").toString();
        TotalTimes = Integer.parseInt(cfg.get("TotalTimes").toString());
        LOCAL_JSON_PATH = cfg.get("LOCAL_JSON_PATH").toString();
        DB_URL = "jdbc:mysql://"+ DB_HOST +":"+DB_PORT+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        in.close();
        in=null;
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
