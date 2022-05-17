package com.day.getBazzar;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import com.alibaba.fastjson.*;

/**
 * 用于获取BazzarJSON数据
 */
public class getBazzar {

    public static URL  SB_BAZZAR_API;
    public static JSONObject  SB_BAZZAR_JSON;
    static {
        try {
            SB_BAZZAR_API = new URL("https://api.hypixel.net/skyblock/bazaar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("是否获取成功:"+getBazzarJSON());
        System.out.println("JSON返回值:"+SB_BAZZAR_JSON.get("success"));
        System.out.println("JSON更新时间:"+toolClass.timestampToString((Long) SB_BAZZAR_JSON.get("lastUpdated")));
    }
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