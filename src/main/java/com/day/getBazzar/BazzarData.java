package com.day.getBazzar;

import static com.day.getBazzar.GlobalVar.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;


/**
 * 关于Bazzar数据处理
 */
public class BazzarData {

    static int id_row_nm = 1;
    static int id_row_day = 1;

    /**
     * 初始化数据库链接操作，并将返回的Connection变量返回回去
     *
     * @param flag 是否输出链接成功消息
     * @return 数据库链接的Connection；
     */
    public static Connection InitializationAndConnection(boolean flag) {
        try {
            Connection conn;
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            if (flag) {
                System.out.println("数据库连接成功！");
            }
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 进行数据库和表格格式的初始化
     * 初始化为分钟统计表格
     * 使用本地JSON文件进行读取
     *
     * @return 是否初始化成功
     */
    public static boolean InitializedDBandTable() {
        System.out.println("正在初始化数据库:" + DB_NM);
        try (Connection conn = InitializationAndConnection(true); Statement statement = conn.createStatement()) {
            //创建数据库
            statement.execute("CREATE DATABASE if not exists " + DB_NM);
            //创建表
            statement.execute("USE " + DB_NM);
            //这里使用本地旧数据进行初始化数据库，无需向API再申请一次JSON
            //若本地读取为空，则自动向API读取.
            JSONObject SB_BAZZAR_JSON_FULL_LOCAL;
            if (ToolClass.LoadLocalJSON(LOCAL_JSON_PATH) == null) {
                System.out.println("检测到本地文件不存在，正在向API获取数据");
                SB_BAZZAR_JSON_FULL_LOCAL = GetBazzar.getBazzarJSON(false);
            } else {
                SB_BAZZAR_JSON_FULL_LOCAL = ToolClass.LoadLocalJSON(LOCAL_JSON_PATH);
            }
            JSONObject SB_BAZZAR_JSON_PRODUCTS = JSONObject.parseObject(String.valueOf(SB_BAZZAR_JSON_FULL_LOCAL.get("products")));
            Set<String> products_list = SB_BAZZAR_JSON_PRODUCTS.keySet();
            for (String products_name : products_list) {
                products_name = CheckIfSpecialItem(products_name);
                String sql = "CREATE TABLE IF NOT EXISTS " + '`' + products_name + '`' +
                        "(\n" +
                        "    `buyPrice`       DOUBLE(25, 6) NOT NULL DEFAULT 0,\n" +
                        "   `sellPrice`      DOUBLE(25, 6) NOT NULL DEFAULT 0,\n" +
                        "    `sellVolume`     INT           NOT NULL DEFAULT 0,\n" +
                        "    `buyVolume`      INT           NOT NULL DEFAULT 0,\n" +
                        "    `sellMovingWeek` BIGINT        NOT NULL DEFAULT 0,\n" +
                        "    `buyMovingWeek`  BIGINT        NOT NULL DEFAULT 0,\n" +
                        "    `sellOrders`     INT           NOT NULL DEFAULT 0,\n" +
                        "    `buyOrders`      INT           NOT NULL DEFAULT 0,\n" +
                        "    `timeStamp`      BIGINT     NOT NULL ,\n" +
                        "    `LowestBuyOderPrice`       DOUBLE(25, 6) ,\n" +
                        "    `HighestSellOderPrice`      DOUBLE(25, 6) ,\n" +
                        "     id int auto_increment not null,\n" +
                        "     primary key (id)\n" +
                        ")";
                statement.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return InitializedDBandTable_statistics(DB_DAY) && InitializedDBandTable_statistics(DB_WK) && InitializedDBandTable_statistics(DB_MO);
    }

    /**
     * 进行数据库和表格格式的初始化
     * 用于统计总结版本
     * 使用本地JSON文件进行读取
     *
     * @param DB_name 对应的数据名字
     * @return 是否初始化成功
     */
    public static boolean InitializedDBandTable_statistics(String DB_name) {
        System.out.println("正在初始化数据库:" + DB_name);
        try (Connection conn = InitializationAndConnection(false); Statement statement = conn.createStatement()) {
            //创建数据库
            statement.execute("CREATE DATABASE if not exists " + DB_name);
            //创建表
            statement.execute("USE " + DB_name);
            //这里使用本地旧数据进行初始化数据库，无需向API再申请一次JSON
            //若本地读取为空，则自动向API读取.
            JSONObject SB_BAZZAR_JSON_FULL_LOCAL = GetBazzar.getBazzarJSON(false);
            JSONObject SB_BAZZAR_JSON_PRODUCTS = JSONObject.parseObject(String.valueOf(SB_BAZZAR_JSON_FULL_LOCAL.get("products")));
            Set<String> products_list = SB_BAZZAR_JSON_PRODUCTS.keySet();
            for (String products_name : products_list) {
                products_name = CheckIfSpecialItem(products_name);
                String sql = "CREATE TABLE IF NOT EXISTS " + '`' + products_name + '`' +
                        "(\n" +
                        "    `buyPriceAvg`       DOUBLE(25, 6) NOT NULL DEFAULT 0,\n" +
                        "    `sellPriceAvg`      DOUBLE(25, 6) NOT NULL DEFAULT 0,\n" +
                        "    `buyVolumeAvg`     INT           NOT NULL DEFAULT 0,\n" +
                        "    `sellVolumeAvg`      INT           NOT NULL DEFAULT 0,\n" +
                        "    `timeStamp`      BIGINT     NOT NULL ,\n" +
                        "    `LowestBuyOderPriceAvg`       DOUBLE(25, 6) ,\n" +
                        "    `HighestSellOderPriceAvg`      DOUBLE(25, 6) ,\n" +
                        "    `MinBuyOrderPrice`     INT           NOT NULL DEFAULT 0,\n" +
                        "    `MaxSellOrderPrice`      INT           NOT NULL DEFAULT 0,\n" +
                        "     id int auto_increment not null,\n" +
                        "     primary key (id)\n" +
                        ")";
                statement.execute(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 用于持续从API当中更新的数据(quick_status分类下的数据)
     *
     * @throws SQLException 抛出SQL数据库异常
     */
    public static void KeepUpdateBazzarData_quick(JSONObject SB_BAZZAR_JSON_FULL) throws Exception {
        //若数据满足阈值，则开始添加数据到统计表格中1 2 3 4 5 6 7
        if (id_row_nm == statistics_day + 1) {
            KeepUpdateBzData_day(SB_BAZZAR_JSON_FULL, DB_DAY);
            id_row_nm = 1;
        } else if (id_row_day % (statistics_week +1) == 0) {
            KeepUpdateBzData_day(SB_BAZZAR_JSON_FULL, DB_WK);
        } else if (id_row_day % (statistics_month +1) == 0) {
            KeepUpdateBzData_day(SB_BAZZAR_JSON_FULL, DB_MO);
        } else {
            Connection conn = InitializationAndConnection(false);
            Statement statement = conn.createStatement();
            statement.execute("USE " + DB_NM);
            JSONObject SB_BAZZAR_JSON_PRODUCTS = JSONObject.parseObject(String.valueOf(SB_BAZZAR_JSON_FULL.get("products")));
            Set<String> products_list = SB_BAZZAR_JSON_PRODUCTS.keySet();
            PreparedStatement pstmt = null;
            for (String products_name : products_list) {
                JSONObject products_quick_status = (JSONObject) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("quick_status");
                Object sellOrder_pricePerUnit = null;
                Object buyOrder_pricePerUnit = null;
                //在特殊情况下，某些物品的订单为空，所以得加一些判断
                if (!((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("buy_summary")).isEmpty()) {
                    JSONObject products_sell_summary_first = (JSONObject) ((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("buy_summary")).get(0);
                    sellOrder_pricePerUnit = products_sell_summary_first.get("pricePerUnit");
                }
                if (!((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("sell_summary")).isEmpty()) {
                    JSONObject products_buy_summary_first = (JSONObject) ((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("sell_summary")).get(0);
                    buyOrder_pricePerUnit = products_buy_summary_first.get("pricePerUnit");
                }
                //因为窒息的防SQL注入，预编译的sql对符号的改动特别蛋疼，所以这里只能修改物品id以此兼容
                products_name = CheckIfSpecialItem(products_name);
                Object buyPrice = products_quick_status.get("buyPrice");
                Object sellPrice = products_quick_status.get("sellPrice");
                Object sellVolume = products_quick_status.get("sellVolume");
                Object buyVolume = products_quick_status.get("buyVolume");
                Object sellMovingWeek = products_quick_status.get("sellMovingWeek");
                Object buyMovingWeek = products_quick_status.get("buyMovingWeek");
                Object sellOrders = products_quick_status.get("sellOrders");
                Object buyOrders = products_quick_status.get("buyOrders");
                Object timeStamp = SB_BAZZAR_JSON_FULL.get("lastUpdated");
                String sql = "INSERT INTO " + products_name + "(" +
                        "buyPrice,sellPrice,sellVolume," +
                        "buyVolume,sellMovingWeek,buyMovingWeek," +
                        "sellOrders,buyOrders,timeStamp,LowestBuyOderPrice,HighestSellOderPrice)" +
                        "VALUES( ?,?,?,?,?,?,?,?,?,?,?)";
                String sql2 = "UPDATE " + products_name +
                        " SET buyPrice = ?,sellPrice = ?,sellVolume=?,buyVolume=?,\n" +
                        "sellMovingWeek=?,buyMovingWeek=?,sellOrders=?,buyOrders=?,\n" +
                        "timeStamp=?,LowestBuyOderPrice=?,HighestSellOderPrice=? where id =" + id_row_nm;
                //当插入次数大于1440次(即一天后)开始对表格数据进行更新操作,减少数据堆积操作
                if (GetBazzar.times >= 1441) {
                    pstmt = conn.prepareStatement(sql2);
                } else {
                    pstmt = conn.prepareStatement(sql);
                }
                //对sql操作进行事务管理，一旦出错进行回滚
                try {
                    conn.setAutoCommit(false);
                    pstmt.setObject(1, buyPrice);
                    pstmt.setObject(2, sellPrice);
                    pstmt.setObject(3, sellVolume);
                    pstmt.setObject(4, buyVolume);
                    pstmt.setObject(5, sellMovingWeek);
                    pstmt.setObject(6, buyMovingWeek);
                    pstmt.setObject(7, sellOrders);
                    pstmt.setObject(8, buyOrders);
                    pstmt.setLong(9, (Long) timeStamp);
                    pstmt.setObject(10, buyOrder_pricePerUnit);
                    pstmt.setObject(11, sellOrder_pricePerUnit);
                    pstmt.execute();
                    conn.commit();
                } catch (Exception e) {
                    conn.rollback();
                    e.printStackTrace();
                    pstmt.close();
                    statement.close();
                    conn.close();
                }
            }
            statement.close();
            pstmt.close();
            conn.close();
            id_row_nm++;
        }
    }

    /**
     * 数据累计一天后进行统计,并存入新的表格当中
     *
     * @param DB_NAME             操作的数据库名称
     * @param SB_BAZZAR_JSON_FULL SB返回的API文件,用于提取获取时间
     * @throws Exception
     */
    public static void KeepUpdateBzData_day(JSONObject SB_BAZZAR_JSON_FULL, String DB_NAME) throws Exception {
        Set<String> products_list = JSONObject.parseObject(String.valueOf(SB_BAZZAR_JSON_FULL.get("products"))).keySet();
        Connection conn = InitializationAndConnection(false);
        Statement statement = conn.createStatement();
        statement.execute("USE " + DB_NAME);
        PreparedStatement pstmt = null;
        List<Object> dataList;
        try {
            for (String products_name : products_list) {
                products_name = CheckIfSpecialItem(products_name);
                if (DB_NAME.equals(DB_DAY)) {
                    dataList = statisticsData(products_name);
                } else {
                    dataList = statisticsData_day(products_name, DB_NAME);
                }
                if (dataList == null) {
                    throw new Exception("dataList is empty");
                }
                String sql2 = "INSERT INTO " + products_name + "(" +
                        "buyPriceAvg,sellPriceAvg,sellVolumeAvg," +
                        "buyVolumeAvg,timeStamp,LowestBuyOderPriceAvg," +
                        "HighestSellOderPriceAvg,MinBuyOrderPrice,MaxSellOrderPrice)" +
                        "VALUES( ?,?,?,?,?,?,?,?,?)";
                pstmt = conn.prepareStatement(sql2);
                //对sql操作进行事务管理，一旦出错进行回滚
                conn.setAutoCommit(false);
                pstmt.setObject(1, dataList.get(0));
                pstmt.setObject(2, dataList.get(1));
                pstmt.setObject(3, dataList.get(2));
                pstmt.setObject(4, dataList.get(3));
                pstmt.setLong(5, (Long) SB_BAZZAR_JSON_FULL.get("lastUpdated"));
                pstmt.setObject(6, dataList.get(4));
                pstmt.setObject(7, dataList.get(5));
                pstmt.setObject(8, dataList.get(6));
                pstmt.setObject(9, dataList.get(7));
                pstmt.execute();
                conn.commit();
            }
        } catch (Exception e) {
            conn.rollback();
            e.printStackTrace();
        } finally {
            pstmt.close();
            statement.close();
            conn.close();
        }
        if(DB_NAME.equals(DB_DAY)){
            id_row_day++;
        }
    }

    /**
     * 对物品进行统计，并将统计好的数据以List集合方式返回
     *
     * @param product_name
     * @return 包含所有统计数据的集合
     */
    public static List<Object> statisticsData(String product_name) {
        List<Float> buyPrices = new ArrayList<>();
        List<Float> sellPrices = new ArrayList<>();
        List<Integer> buyVolumes = new ArrayList<>();
        List<Integer> sellVolumes = new ArrayList<>();
        List<Float> lBuyOrderPrices = new ArrayList<>();
        List<Float> hSellOrderPrices = new ArrayList<>();
        List<Object> optObject = new ArrayList<>();
        List<Object> statData = new ArrayList<>();
        try {
            Connection conn = InitializationAndConnection(false);
            Statement statement = conn.createStatement();
            statement.execute("USE " + DB_NM);
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM " + product_name;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                buyPrices.add((float) rs.getDouble("buyPrice"));
                sellPrices.add((float) rs.getDouble("sellPrice"));
                buyVolumes.add(rs.getInt("buyVolume"));
                sellVolumes.add(rs.getInt("sellVolume"));
                lBuyOrderPrices.add((float) rs.getDouble("LowestBuyOderPrice"));
                hSellOrderPrices.add((float) rs.getDouble("HighestSellOderPrice"));
            }
            OptionalDouble od_BuyPrices = buyPrices.stream().mapToDouble(num -> num).average();
            OptionalDouble od_SellPrices = sellPrices.stream().mapToDouble(num -> num).average();
            OptionalDouble od_BuyVolumes = buyVolumes.stream().mapToDouble(num -> num).average();
            OptionalDouble od_SellVolumes = sellVolumes.stream().mapToDouble(num -> num).average();
            OptionalDouble od_BuyOrderPricesAvg = lBuyOrderPrices.stream().mapToDouble(num -> num).average();
            OptionalDouble od_SellOrderPricesAvg = hSellOrderPrices.stream().mapToDouble(num -> num).average();
            OptionalDouble od_lBuyOrderPrices = lBuyOrderPrices.stream().mapToDouble(num -> num).min();
            OptionalDouble od_hSellOrderPrices = hSellOrderPrices.stream().mapToDouble(num -> num).max();
            optObject.add(od_BuyPrices);
            optObject.add(od_SellPrices);
            optObject.add(od_SellVolumes);
            optObject.add(od_BuyVolumes);
            optObject.add(od_BuyOrderPricesAvg);
            optObject.add(od_SellOrderPricesAvg);
            optObject.add(od_lBuyOrderPrices);
            optObject.add(od_hSellOrderPrices);
            for (Object o : optObject) {
                OptionalDouble od = (OptionalDouble) o;
                if (od.isPresent()) {
                    statData.add(od.getAsDouble());
                }
            }
            statement.close();
            stmt.close();
            conn.close();
            return statData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对物品进行统计，并将统计好的数据以List集合方式返回
     * 以day为单位进行统计
     *
     * @param product_name 传入物品name
     * @param DB_NAME      需要进行统计的数据库
     * @return 包含所有统计数据的集合
     */
    public static List<Object> statisticsData_day(String product_name, String DB_NAME) {
        List<Float> buyPrices = new ArrayList<>();
        List<Float> sellPrices = new ArrayList<>();
        List<Integer> buyVolumes = new ArrayList<>();
        List<Integer> sellVolumes = new ArrayList<>();
        List<Float> lBuyOrderPrices = new ArrayList<>();
        List<Float> hSellOrderPrices = new ArrayList<>();
        List<Float> minBuyOrderPrices = new ArrayList<>();
        List<Float> maxSellOrderPrices = new ArrayList<>();
        List<Object> optObject = new ArrayList<>();
        List<Object> statData = new ArrayList<>();
        int limit = 0;
        if(DB_NAME.equals(DB_WK)){
            limit = statistics_week;
        }
        if(DB_NAME.equals(DB_MO)){
            limit = statistics_month;
        }
        try {
            Connection conn = InitializationAndConnection(false);
            Statement statement = conn.createStatement();
            statement.execute("USE " + DB_DAY);
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM " + product_name +" order by id desc limit 0,"+limit;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                buyPrices.add((float) rs.getDouble("buyPriceAvg"));
                sellPrices.add((float) rs.getDouble("sellPriceAvg"));
                buyVolumes.add(rs.getInt("buyVolumeAvg"));
                sellVolumes.add(rs.getInt("sellVolumeAvg"));
                lBuyOrderPrices.add((float) rs.getDouble("LowestBuyOderPriceAvg"));
                hSellOrderPrices.add((float) rs.getDouble("HighestSellOderPriceAvg"));
                minBuyOrderPrices.add((float) rs.getDouble("MinBuyOderPriceAvg"));
                maxSellOrderPrices.add((float) rs.getDouble("MaxSellOderPriceAvg"));
            }
            OptionalDouble od_BuyPrices = buyPrices.stream().mapToDouble(num -> num).average();
            OptionalDouble od_SellPrices = sellPrices.stream().mapToDouble(num -> num).average();
            OptionalDouble od_BuyVolumes = buyVolumes.stream().mapToDouble(num -> num).average();
            OptionalDouble od_SellVolumes = sellVolumes.stream().mapToDouble(num -> num).average();
            OptionalDouble od_BuyOrderPricesAvg = lBuyOrderPrices.stream().mapToDouble(num -> num).average();
            OptionalDouble od_SellOrderPricesAvg = hSellOrderPrices.stream().mapToDouble(num -> num).average();
            OptionalDouble od_minBuyOrderPrices = minBuyOrderPrices.stream().mapToDouble(num -> num).min();
            OptionalDouble od_maxSellOrderPrices = maxSellOrderPrices.stream().mapToDouble(num -> num).max();
            optObject.add(od_BuyPrices);
            optObject.add(od_SellPrices);
            optObject.add(od_SellVolumes);
            optObject.add(od_BuyVolumes);
            optObject.add(od_BuyOrderPricesAvg);
            optObject.add(od_SellOrderPricesAvg);
            optObject.add(od_minBuyOrderPrices);
            optObject.add(od_maxSellOrderPrices);
            for (Object o : optObject) {
                OptionalDouble od = (OptionalDouble) o;
                if (od.isPresent()) {
                    statData.add(od.getAsDouble());
                }
            }
            statement.close();
            stmt.close();
            conn.close();
            return statData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用于检测物品名字是否含有特殊字符
     * 例如ink_sack:3等含有副id的特殊物品
     * 若存在则进行修改，否则返回原字符串
     *
     * @param products_name 可能存在特殊字符的物品name
     * @return 返回修改后的物品name
     */
    public static String CheckIfSpecialItem(String products_name) {
        //紧急添加:因为原数据存在log:2与log_2，所以在此将两者区分。改成log_2→log_2_1,log:2→log_2,log_2:1→log_2_2
        if ((products_name).equalsIgnoreCase("log_2")) {
            return "log_2_1";
        }
        if ((products_name).equalsIgnoreCase("log_2:1")) {
            return "log_2_2";
        }
        if (products_name.contains(":")) {
            return products_name.replace(":", "_");
        } else return products_name;
    }
}