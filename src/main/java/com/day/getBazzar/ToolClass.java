package com.day.getBazzar;


import com.alibaba.fastjson.*;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class ToolClass {

    /**
     * 用于将“字符串“形的json格式写入成.json格式文件
     * @param jsonData
     * @param filePath
     * @return
     */
    public static boolean createJsonFile (Object jsonData,String filePath){
        String content = JSON.toJSONString(jsonData,SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
        //标记文件状态
        boolean flag = true;

        try {
            //尝试创建文件
            System.out.println(filePath);
            File jsonFile = new File(filePath);
            if(!jsonFile.getParentFile().exists()){
                jsonFile.getParentFile().mkdirs();}
            if(jsonFile.exists())
                jsonFile.delete();
            if(jsonFile.createNewFile()){
                System.out.println("创建成功");
            } else {
                System.out.println("创建失败");
                return false;
            }
            //准备写入
            Writer write = new OutputStreamWriter(new FileOutputStream(jsonFile),"UTF-8");
            write.write(content);
            write.flush();
            write.close();
        } catch (Exception e ){
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    public static JSONObject LoadLocalJSON (String filePath){
        String s = ToolClass.readJsonFile(filePath);
        return JSON.parseObject(s);
    }

    /**
     * 读取json文件，返回json串
     * @param fileName
     * @return
     */
    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);

            Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            System.out.println("无法读取文件!");
            return null;
        }
    }

    /**
     * 返回格式化的json字符串
     * @param json json对象
     * @return
     */
    public static String formatJsonString(JSONObject json){
        return  JSON.toJSONString(json, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
    }

    /**
     * 将时间戳格式转化成当日时间
     * @param timeStamp 时间戳
     * @return 返回String类型时间(时:分:秒)
     */
    public static String timestampToTimes (Long timeStamp) {
        //将时间戳转化为固定格式的日期
        SimpleDateFormat times =  new SimpleDateFormat("HH:mm:ss");
        return times.format(timeStamp);
    }

    /**
     * 将时间戳格式转化成完整的日期
     * @param timeStamp 时间戳
     * @return 返回String类型的日期(年-月-日 时:分:秒)
     */
    public static String timestampToDate (Long timeStamp) {
        //将时间戳转化为固定格式的日期
        SimpleDateFormat Date =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return Date.format(timeStamp);
    }
    /**
     * 判断是否为整数
     * @param str 传入的字符串
     * @return 是整数返回true,否则返回false
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
}