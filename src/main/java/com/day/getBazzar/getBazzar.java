package com.day.getBazzar;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import com.alibaba.fastjson.*;

/**
 * 用于获取BazzarJSON数据
 */
public class getBazzar {

    public static URL  SB_BAZZAR_API;
    public static JSONObject  SB_BAZZAR_JSON;
    static int times = 0;
    static {
        try {
            SB_BAZZAR_API = new URL("https://api.hypixel.net/skyblock/bazaar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用于反复执行更新数据操作的timerTask
     */
    static class continuedGet extends TimerTask{
        @Override
        public void run() {
            System.out.println("-------第"+(times+1)+"次获取-------");
            JSONObject SB_BAZZAR_JSON = getBazzarJSON();
            System.out.println("JSONAPIGet返回值:"+SB_BAZZAR_JSON.get("success"));
            System.out.println("JSON更新时间:"+toolClass.timestampToDate((Long) SB_BAZZAR_JSON.get("lastUpdated")));
            try {
                BazzarData.KeepUpdateBazzarData_quick();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            System.out.println("--------完成数据导入----------");
            if(times == 10){
                cancel();
            } else
                times++;
        }
    }

    //拟运行主类
    public static void main(String[] args) {
        BazzarData.InitializationAndConnection();
        BazzarData.InitializedDBandTable();
        Timer timer2 = new Timer();
        timer2.schedule(new continuedGet(), 100, 60000);  //1秒后执行，并且每隔1分钟重复执行
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //timer.cancel();  //终止计时器，放弃所有已安排的任务
            timer2.purge();  //释放内存
    }

    /**
     * 从Hpyixel skyblock api当中获取Bazzar的数据，并将数据作为JSON对象返回
     * @return fastjson的JSONObject：Bazzar的全部数据
     */
    public static JSONObject getBazzarJSON() {
        HttpURLConnection connection = null;
        InputStream inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            connection = (HttpURLConnection)SB_BAZZAR_API.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(500000);
            connection.setConnectTimeout(500000);
            connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            //接受输入
            System.out.println("正在接受API数据:");
            inputStreamReader = connection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStreamReader,"UTF-8"));

            String line;

            while ((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();
            JSONObject json = JSONObject.parseObject(jsonString);
            return json;
           //return createFile.createJsonFile(json,"E:\\modding\\SmallProject\\GetBazzar\\Bazzar.json");

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}