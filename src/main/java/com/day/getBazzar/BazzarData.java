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



    //JSONObject json =
    static final String JSON_PATH = "E:\\modding\\SmallProject\\GetBazzar\\somedata\\Bazzar.json";
    public static JSONObject SB_BAZZAR_JSON_FULL = toolClass.LoadLocalJSON(JSON_PATH);
    public static JSONObject SB_BAZZAR_JSON_PRODUCTS = JSONObject.parseObject(String.valueOf(SB_BAZZAR_JSON_FULL.get("products")));
    static  String DB_PORT = "3306";
    static final String DB_URL = "jdbc:mysql://localhost:"+DB_PORT+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static  String USER = "root";
    static  String PASSWORD = "kel123";
    static String SQL_DB_QUICK_STATUS = "bz_quick_status";
    static Connection conn = null;
    static Statement statement = null;
    public static void main(String[] args) throws SQLException {
        InitializationAndConnection();
        InitializedDBandTable(SB_BAZZAR_JSON_PRODUCTS);
        System.out.println("Bazzar内所有物品数:"+SB_BAZZAR_JSON_PRODUCTS.size());

    }

    public static void InitializationAndConnection (){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("数据库连接成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean InitializedDBandTable (JSONObject products ) {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            //创建数据库
           //statement.execute("CREATE DATABASE BZ_quick_status");
            //创建表
            statement.execute("USE bz_quick_status");

            Set<String> products_list = products.keySet();
            Iterator<String> products_iterator = products_list.iterator();

           while (products_iterator.hasNext()){
                String sql;

                String products_name = products_iterator.next();
                JSONObject products_quick_status = (JSONObject) ((JSONObject) products.get(products_name)).get("quick_status");
                sql = "CREATE TABLE IF NOT EXISTS "+ '`' +products_name + '`' +
                        "(" +
                        "    `buyPrice`       DOUBLE(25, 6) NOT NULL DEFAULT 0," +
                        "    `sellPrice`      DOUBLE(25, 6) NOT NULL DEFAULT 0," +
                        "    `sellVolume`     INT           NOT NULL DEFAULT 0," +
                        "    `buyVolume`      INT           NOT NULL DEFAULT 0," +
                        "    `sellMovingWeek` BIGINT        NOT NULL DEFAULT 0," +
                        "    `buyMovingWeek`  BIGINT        NOT NULL DEFAULT 0," +
                        "    `sellOrders`     INT           NOT NULL DEFAULT 0," +
                        "    `buyOrders`      INT           NOT NULL DEFAULT 0" +
                        ")";
                statement.execute(sql);
                Object buyPrice = products_quick_status.get("buyPrice");
                BigDecimal sellPrice = (BigDecimal) products_quick_status.get("sellPrice");
                int sellVolume = (int) products_quick_status.get("sellVolume");
                int buyVolume = (int) products_quick_status.get("buyVolume");
                int sellMovingWeek = (int) products_quick_status.get("sellMovingWeek");
                int buyMovingWeek = (int) products_quick_status.get("buyMovingWeek");
                int sellOrders = (int) products_quick_status.get("sellOrders");
                int buyOrders = (int) products_quick_status.get("buyOrders");
                String sql2 =
                        "INSERT INTO "+ '`' +products_name + '`' +"(" +
                        "buyPrice,sellPrice,sellVolume," +
                        "buyVolume,sellMovingWeek,buyMovingWeek," +
                        "sellOrders,buyOrders)"+
                        "VALUES("+ buyPrice +","+ sellPrice +","+ sellVolume +","+ buyVolume +","+ sellMovingWeek +","+ buyMovingWeek +","+ sellOrders +","+buyOrders +")";
                //System.out.println(sql2);
                statement.execute(sql2);
                //System.out.println(products_iterator.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
                statement.close();
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

}