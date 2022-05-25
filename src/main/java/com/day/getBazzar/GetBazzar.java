package com.day.getBazzar;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import com.alibaba.fastjson.*;

import static com.day.getBazzar.GlobalVar.*;

/**
 * 用于获取BazzarJSON数据
 */
public class GetBazzar {

    static int times=0;

    /**
     * 用于反复执行更新数据操作的timerTask
     */
    static class continuedGet extends TimerTask{
        @Override
        public void run() {
            System.out.println("-------第"+(times+1)+"次获取-------");
            JSONObject SB_BAZZAR_JSON = getBazzarJSON();
            System.out.println("JSONAPIGet返回值:"+SB_BAZZAR_JSON.get("success"));
            System.out.println("JSON更新时间:"+ToolClass.timestampToDate((Long) SB_BAZZAR_JSON.get("lastUpdated")));
            try {
                BazzarData.KeepUpdateBazzarData_quick(SB_BAZZAR_JSON);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("--------完成数据导入----------");
            if(times == TotalTimes-1){
                cancel();
            } else
                times++;
        }
    }

    //拟运行主类
    public static void main(String[] args) throws IOException {
        //读入参数，这里创建对象是因为静态方法会导致线程一直运行
        GlobalVar gv = new GlobalVar();
        gv.readConfig();

        BazzarData.InitializedDBandTable();
        Timer timer2 = new Timer();
        timer2.schedule(new continuedGet(), 100, 15000);  //1秒后执行，并且每隔1分钟重复执行
        //这里不是循环结束后运行的代码，循环任务会分开成一个子线程运行。

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
            System.out.println("获取完成");
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