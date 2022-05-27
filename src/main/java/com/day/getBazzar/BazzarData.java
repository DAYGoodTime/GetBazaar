package com.day.getBazzar;

import static com.day.getBazzar.GlobalVar.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Iterator;
import java.util.Set;


/**
 * 关于Bazzar数据处理
 */
public class BazzarData {

    static String SQL_DB_QUICK_STATUS = "bz_quick_status";
    //用于测试的main方法
//    public static void main(String[] args) throws SQLException {
//        InitializedDBandTable();
//        KeepUpdateBazzarData_quick();
//        System.out.println("Bazzar内所有物品数:"+SB_BAZZAR_JSON_PRODUCTS.size());
//    }

    /**
     * 初始化数据库链接操作，并将返回的Connection变量返回回去
     * @return  数据库链接的Connection；
     */
    public static Connection InitializationAndConnection (){
        try {
            Connection conn;
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
        System.out.println("正在初始化数据库:");
        Statement statement = null;
        Connection conn = InitializationAndConnection();
        try {
            statement = conn.createStatement();
            //创建数据库
            statement.execute("CREATE DATABASE if not exists bz_quick_status");
            //创建表
            statement.execute("USE bz_quick_status");
            //这里使用本地旧数据进行初始化数据库，无需向API再申请一次JSON
            JSONObject SB_BAZZAR_JSON_FULL_LOCAL = ToolClass.LoadLocalJSON(LOCAL_JSON_PATH);
            JSONObject SB_BAZZAR_JSON_PRODUCTS = JSONObject.parseObject(String.valueOf(SB_BAZZAR_JSON_FULL_LOCAL.get("products")));
            Set<String> products_list = SB_BAZZAR_JSON_PRODUCTS.keySet();
            for (String products_name : products_list) {
                String sql;
                sql = "CREATE TABLE IF NOT EXISTS " + '`' + products_name + '`' +
                        "(" +
                        "    `buyPrice`       DOUBLE(25, 6) NOT NULL DEFAULT 0," +
                        "    `sellPrice`      DOUBLE(25, 6) NOT NULL DEFAULT 0," +
                        "    `sellVolume`     INT           NOT NULL DEFAULT 0," +
                        "    `buyVolume`      INT           NOT NULL DEFAULT 0," +
                        "    `sellMovingWeek` BIGINT        NOT NULL DEFAULT 0," +
                        "    `buyMovingWeek`  BIGINT        NOT NULL DEFAULT 0," +
                        "    `sellOrders`     INT           NOT NULL DEFAULT 0," +
                        "    `buyOrders`      INT           NOT NULL DEFAULT 0," +
                        "    `timeStamp`      BIGINT        NOT NULL DEFAULT 0," +
                        "    `HighestBuyOderPrice`       DOUBLE(25, 6) ," +
                        "    `HighestSellOderPrice`      DOUBLE(25, 6) " +
                        ")";
                statement.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                statement.close();
                conn.close();
                System.out.println("初始化数据库成功");
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
    public static void KeepUpdateBazzarData_quick(JSONObject SB_BAZZAR_JSON_FULL) throws SQLException {
        Connection conn = InitializationAndConnection();
        Statement statement = conn.createStatement();
        statement.execute("USE bz_quick_status");
        JSONObject SB_BAZZAR_JSON_PRODUCTS = JSONObject.parseObject(String.valueOf(SB_BAZZAR_JSON_FULL.get("products")));
        Set<String> products_list = SB_BAZZAR_JSON_PRODUCTS.keySet();
        for (String products_name : products_list) {
            //System.out.println(products_name);
            JSONObject products_quick_status = (JSONObject) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("quick_status");

            Object sellOrder_pricePerUnit = null;
            Object buyOrder_pricePerUnit = null;
            //在特殊情况下，某些物品的订单为空，所以得加一些判断
            if (!((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("sell_summary")).isEmpty()) {
               JSONObject products_sell_summary_first = (JSONObject) ((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("sell_summary")).get(0);
                sellOrder_pricePerUnit = products_sell_summary_first.get("pricePerUnit");
            }
            if (!((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("buy_summary")).isEmpty()) {
                JSONObject products_buy_summary_first = (JSONObject) ((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("buy_summary")).get(0);
                buyOrder_pricePerUnit = products_buy_summary_first.get("pricePerUnit");
            }
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
                    "INSERT INTO " + '`' + products_name + '`' + "(" +
                            "buyPrice,sellPrice,sellVolume," +
                            "buyVolume,sellMovingWeek,buyMovingWeek," +
                            "sellOrders,buyOrders,timeStamp,HighestBuyOderPrice,HighestSellOderPrice)" +
                            "VALUES(" + buyPrice + "," + sellPrice + "," + sellVolume + "," + buyVolume + "," + sellMovingWeek
                            + "," + buyMovingWeek + "," + sellOrders + "," + buyOrders + "," + timeStamp + "," + buyOrder_pricePerUnit + "," + sellOrder_pricePerUnit + ")";
            //对sql操作进行事务管理，一旦出错进行回滚
            try {
                conn.setAutoCommit(false);
                statement.execute(sql2);
                conn.commit();
            } catch (Exception e){
                conn.rollback();
                e.printStackTrace();
            }
        }
        statement.close();
        conn.close();
    }

}