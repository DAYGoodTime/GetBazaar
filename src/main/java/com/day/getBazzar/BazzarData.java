package com.day.getBazzar;

import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Iterator;
import java.util.Set;

/**
 * 关于Bazzar数据处理
 */
public class BazzarData {

    //各种变量:
    static final String JSON_PATH = "E:\\modding\\SmallProject\\GetBazzar\\somedata\\Bazzar.json";
    // 本地JSON用于测试 public static JSONObject SB_BAZZAR_JSON_FULL = toolClass.LoadLocalJSON(JSON_PATH);
    public static JSONObject SB_BAZZAR_JSON_FULL = getBazzar.getBazzarJSON();
    public static JSONObject SB_BAZZAR_JSON_PRODUCTS = JSONObject.parseObject(String.valueOf(SB_BAZZAR_JSON_FULL.get("products")));

    static  String DB_PORT = "3306";
    static final String DB_URL = "jdbc:mysql://localhost:"+DB_PORT+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static  String USER = "root";
    static  String PASSWORD = "kel123";

    static String SQL_DB_QUICK_STATUS = "bz_quick_status";
    static Set<String> products_list = SB_BAZZAR_JSON_PRODUCTS.keySet();
    //用于测试的main方法
    public static void main(String[] args) throws SQLException {
        InitializationAndConnection();
        InitializedDBandTable();
        KeepUpdateBazzarData_quick();
        System.out.println("Bazzar内所有物品数:"+SB_BAZZAR_JSON_PRODUCTS.size());
    }

    /**
     * 初始化数据库链接操作，并将返回的Connection变量返回回去
     * @return  数据库链接的Connection；
     */
    public static Connection InitializationAndConnection (){
        try {
            Connection conn = null;
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("数据库连接成功！");
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 进行数据库和表格格式的初始化
     * 使用本地JSON文件进行读取
     * @return 是否初始化成功
     */
    public static boolean InitializedDBandTable () {
        Statement statement = null;
        Connection conn = InitializationAndConnection();
        try {
            statement = conn.createStatement();
            statement = conn.createStatement();
            //创建数据库
            //statement.execute("CREATE DATABASE BZ_quick_status");
            //创建表
            statement.execute("USE bz_quick_status");
            //这里使用本地旧数据进行初始化数据库，无需向API再申请一次JSON
            JSONObject SB_BAZZAR_JSON_FULL_LOCAL = toolClass.LoadLocalJSON(JSON_PATH);
            JSONObject SB_BAZZAR_JSON_PRODUCTS = JSONObject.parseObject(String.valueOf(SB_BAZZAR_JSON_FULL_LOCAL.get("products")));
            Set<String> products_list = SB_BAZZAR_JSON_PRODUCTS.keySet();
            Iterator<String> products_iterator = products_list.iterator();
            while (products_iterator.hasNext()){
                String sql;
                String products_name = products_iterator.next();
                sql = "CREATE TABLE IF NOT EXISTS "+ '`' +products_name + '`' +
                        "(" +
                        "    `buyPrice`       DOUBLE(25, 6) NOT NULL DEFAULT 0," +
                        "    `sellPrice`      DOUBLE(25, 6) NOT NULL DEFAULT 0," +
                        "    `sellVolume`     INT           NOT NULL DEFAULT 0," +
                        "    `buyVolume`      INT           NOT NULL DEFAULT 0," +
                        "    `sellMovingWeek` BIGINT        NOT NULL DEFAULT 0," +
                        "    `buyMovingWeek`  BIGINT        NOT NULL DEFAULT 0," +
                        "    `sellOrders`     INT           NOT NULL DEFAULT 0," +
                        "    `buyOrders`      INT           NOT NULL DEFAULT 0," +
                        "    `timeStamp`      BIGINT        NOT NULL DEFAULT 0"  +
                        ")";
                statement.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                statement.close();
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 用于持续从API当中更新的数据(quick_status分类下的数据)
     * @throws SQLException 抛出SQL数据库异常
     */
    public static void KeepUpdateBazzarData_quick() throws SQLException {
        Statement statement = null;
        Connection conn = InitializationAndConnection();
        statement = conn.createStatement();
        statement.execute("USE bz_quick_status");
        Iterator<String> products_iterator = products_list.iterator();
        while (products_iterator.hasNext()){
            String products_name = products_iterator.next();
            JSONObject products_quick_status = (JSONObject) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("quick_status");
            Object buyPrice = products_quick_status.get("buyPrice");
            BigDecimal sellPrice = (BigDecimal) products_quick_status.get("sellPrice");
            int sellVolume = (int) products_quick_status.get("sellVolume");
            int buyVolume = (int) products_quick_status.get("buyVolume");
            int sellMovingWeek = (int) products_quick_status.get("sellMovingWeek");
            int buyMovingWeek = (int) products_quick_status.get("buyMovingWeek");
            int sellOrders = (int) products_quick_status.get("sellOrders");
            int buyOrders = (int) products_quick_status.get("buyOrders");
            Object timeStamp = SB_BAZZAR_JSON_FULL.get("lastUpdated");
            String sql2 =
                    "INSERT INTO "+ '`' +products_name + '`' +"(" +
                            "buyPrice,sellPrice,sellVolume," +
                            "buyVolume,sellMovingWeek,buyMovingWeek," +
                            "sellOrders,buyOrders,timeStamp)"+
                            "VALUES("+ buyPrice +","+ sellPrice +","+ sellVolume +","+ buyVolume +","+ sellMovingWeek
                            +","+ buyMovingWeek +","+ sellOrders +","+buyOrders +","+ timeStamp +")";
            statement.execute(sql2);
        }
        statement.close();
    }

}