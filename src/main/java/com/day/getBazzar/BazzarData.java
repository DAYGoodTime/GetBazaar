package com.day.getBazzar;

import static com.day.getBazzar.GlobalVar.*;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.db.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * 关于Bazzar数据处理
 */
public class BazzarData {

    static int id_row_nm = 1;
    static int id_row_day = 1;
    private static final Log log = LogFactory.get();

    /**
     * 进行数据库和表格格式的初始化
     *
     * @return 是否初始化成功
     */
    public static boolean InitializedDBandTable() {
        log.info("正在初始化数据库");
        try{
            //创建数据库
            log.info("正在创建数据库:{}",DB_NAME);
            Db.use(bazzar_ds).execute("CREATE DATABASE if not exists " + DB_NAME);
            //创建表
            log.info("正在初始化表格:{}",TB_NM);
                String sql = "CREATE TABLE IF NOT EXISTS " + '`' + TB_NM + '`' +
                        "(\n" +
                        "    `products_name`  VARCHAR(255)   ,\n" +
                        "    `buyPrice`       DOUBLE(25, 6)  DEFAULT 0,\n" +
                        "    `sellPrice`      DOUBLE(25, 6)  DEFAULT 0,\n" +
                        "    `sellVolume`     INT            DEFAULT 0,\n" +
                        "    `buyVolume`      INT            DEFAULT 0,\n" +
                        "    `sellMovingWeek` BIGINT         DEFAULT 0,\n" +
                        "    `buyMovingWeek`  BIGINT         DEFAULT 0,\n" +
                        "    `sellOrders`     INT            DEFAULT 0,\n" +
                        "    `buyOrders`      INT            DEFAULT 0,\n" +
                        "    `timeStamp`      BIGINT     NOT NULL ,\n" +
                        "    `LowestBuyOderPrice`       DOUBLE(25, 6) DEFAULT 0 ,\n" +
                        "    `HighestSellOderPrice`      DOUBLE(25, 6) DEFAULT 0,\n" +
                        "     uniqueId int auto_increment not null,\n" +
                        "     id int not null,\n" +
                        "     INDEX (products_name(255))  ,\n" +
                        "     primary key (uniqueId)\n" +
                        ")";
                Db.use(bazzar_ds).execute(sql);
        } catch (SQLException e) {
            log.error("数据库初始化错误:",e);
            return false;
        }
        return InitializedTable(TB_DAY) && InitializedTable(TB_WK) && InitializedTable(TB_MO);
    }

    /**
     * 进行表格格式的初始化
     * 用于统计总结版本
     * @param TB_name 对应的数据名字
     * @return 是否初始化成功
     */
    private static boolean InitializedTable(String TB_name) {
        try {
            log.info("正在初始化表格:{}",TB_name);
            //创建表
                String sql = "CREATE TABLE IF NOT EXISTS " + '`' + TB_name + '`' +
                        "(\n" +
                        "    `products_name`     VARCHAR(255)  ,\n" +
                        "    `buyPriceAvg`       DOUBLE(25, 6) DEFAULT 0,\n" +
                        "    `sellPriceAvg`      DOUBLE(25, 6) DEFAULT 0,\n" +
                        "    `buyVolumeAvg`      INT           DEFAULT 0,\n" +
                        "    `sellVolumeAvg`     INT           DEFAULT 0,\n" +
                        "    `timeStamp`         BIGINT        NOT NULL ,\n" +
                        "    `LowestBuyOderPriceAvg`           DOUBLE(25, 6) ,\n" +
                        "    `HighestSellOderPriceAvg`         DOUBLE(25, 6) ,\n" +
                        "    `MinBuyOrderPrice`       INT      DEFAULT 0,\n" +
                        "    `MaxSellOrderPrice`      INT      DEFAULT 0,\n" +
                        "     INDEX (products_name(255))  ,\n" +
                        "     uniqueId int auto_increment not null,\n" +
                        "     id int not null,\n" +
                        "     primary key (uniqueId)\n" +
                        ")";
                Db.use(bazzar_ds).execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 用于持续从API当中更新的数据(quick_status分类下的数据)
     */
    public static void KeepUpdateBazzarData_quick(JSONObject SB_BAZZAR_JSON_FULL) {
        //若数据满足阈值，则开始添加数据到统计表格中
        try{
            if (id_row_nm == statistics_day + 1) {
                KeepUpdateBzData_day(SB_BAZZAR_JSON_FULL, TB_DAY);
                id_row_nm = 1;
            } else if (id_row_day % (statistics_week +1) == 0) {
                KeepUpdateBzData_day(SB_BAZZAR_JSON_FULL, TB_WK);
            } else if (id_row_day % (statistics_month +1) == 0) {
                KeepUpdateBzData_day(SB_BAZZAR_JSON_FULL, TB_MO);
            } else {
                JSONObject SB_BAZZAR_JSON_PRODUCTS = JSONObject.parseObject(String.valueOf(SB_BAZZAR_JSON_FULL.get("products")));
                Set<String> products_list = SB_BAZZAR_JSON_PRODUCTS.keySet();
                for (String products_name : products_list) {
                    JSONObject products_quick_status = (JSONObject) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("quick_status");
                    Object LowestBuyOderPrice = null;
                    Object HighestSellOderPrice = null;
                    //在特殊情况下，某些物品的订单为空，所以得加一些判断
                    if (!((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("buy_summary")).isEmpty()) {
                        JSONObject products_sell_summary_first = (JSONObject) ((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("buy_summary")).get(0);
                        LowestBuyOderPrice = products_sell_summary_first.get("pricePerUnit");
                    }
                    if (!((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("sell_summary")).isEmpty()) {
                        JSONObject products_buy_summary_first = (JSONObject) ((JSONArray) ((JSONObject) SB_BAZZAR_JSON_PRODUCTS.get(products_name)).get("sell_summary")).get(0);
                        HighestSellOderPrice = products_buy_summary_first.get("pricePerUnit");
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
                    //对sql操作进行事务管理，一旦出错进行回滚
                    Session session = Session.create(bazzar_ds);
                    try {
                        session.beginTransaction();
                        Entity entity = Entity.create(TB_NM).set("products_name",products_name).set("buyPrice",buyPrice).set("sellPrice", sellPrice).set("sellVolume",sellVolume)
                                .set("buyVolume",buyVolume).set("sellMovingWeek",sellMovingWeek).set("buyMovingWeek",buyMovingWeek).set("sellOrders",sellOrders)
                                .set("buyOrders",buyOrders).set("timeStamp",timeStamp).set("LowestBuyOderPrice",LowestBuyOderPrice).set("HighestSellOderPrice",HighestSellOderPrice)
                                .set("id",id_row_nm);
                        //当插入次数大于1440次(即一天后)开始对表格数据进行更新操作,减少数据堆积操作
                        if (GetBazzar.times >= 1441) {
                            session.update(
                                    entity,
                                    Entity.create().set("id",id_row_nm)
                            );
                        } else {
                            session.insert(
                                    entity
                            );
                        }
                        session.commit();
                    } catch (SQLException e) {
                        log.error("API数据输入异常，数据已回滚");
                        session.quietRollback();
                    }
                }
                id_row_nm++;
            }
        } catch (SQLException e){
            log.error("数据库统计错误{}",e);
        }

    }

    /**
     * 数据累计一天后进行统计,并存入新的表格当中
     *
     * @param TB_NAME             操作的表名称
     * @param SB_BAZZAR_JSON_FULL SB返回的API文件,用于提取获取时间
     */
    public static void KeepUpdateBzData_day(JSONObject SB_BAZZAR_JSON_FULL, String TB_NAME) throws SQLException {
        Set<String> products_list = JSONObject.parseObject(String.valueOf(SB_BAZZAR_JSON_FULL.get("products"))).keySet();
        List<Object> dataList;
        Session session = Session.create(bazzar_ds);
        try {
            for (String products_name : products_list) {
                session.beginTransaction();
                products_name = CheckIfSpecialItem(products_name);
                if (TB_NAME.equals(TB_DAY)) {
                    dataList = statisticsData(products_name);
                } else {
                    dataList = statisticsData_day(products_name, TB_NAME);
                }
                if (dataList == null) {
                    log.error("数据列表为空");
                    throw new Exception("dataList is empty");
                }
                Entity entity = Entity.create(TB_NAME).set("products_name",products_name).set("buyPriceAvg",dataList.get(0)).set("sellPriceAvg", dataList.get(1))
                        .set("sellVolumeAvg",dataList.get(2)).set("buyVolumeAvg",dataList.get(3)).set("timeStamp",SB_BAZZAR_JSON_FULL.get("lastUpdated"))
                        .set("LowestBuyOderPriceAvg",dataList.get(4)).set("HighestSellOderPriceAvg",dataList.get(5))
                        .set("MinBuyOrderPrice",dataList.get(6)).set("MaxSellOrderPrice",dataList.get(7))
                        .set("id",id_row_day);
                //对sql操作进行事务管理，一旦出错进行回滚
                session.insert(entity);
                session.commit();
            }
        } catch (Exception e) {
            log.error("SQL操作错误:{}",e);
            session.quietRollback();
        }
        if(TB_NAME.equals(TB_DAY)){
            id_row_day++;
        }
    }

    /**
     * 对物品进行统计，并将统计好的数据以List集合方式返回
     * 统计单位:分钟,结果:日 结果
     * @param product_name 传入物品name
     * @return 包含所有统计数据的List集合
     */
    public static List<Object> statisticsData(String product_name) {
        List<Object> result = new ArrayList<>();
        String [] colum_list = {"buyPrice","sellPrice","buyVolume",
                                "sellVolume","LowestBuyOderPrice","HighestSellOderPrice"};
        try {
            for (String colum_name: colum_list) {
                String sql = "SELECT AVG("+colum_name+") FROM "+ TB_NM +" where products_name = ?";
                List<Entity> entitys =Db.use(bazzar_ds).query(sql,product_name);
                result.add(entitys.get(0).get("AVG("+colum_name+")"));
            }
            String sqlMin = "SELECT Min(LowestBuyOderPrice) FROM "+ TB_NM +" where products_name = ?";
            result.add((Db.use(bazzar_ds).query(sqlMin,product_name)).get(0).get("Min(LowestBuyOderPrice)"));
            String sqlMax = "SELECT MAX(HighestSellOderPrice) FROM "+ TB_NM +" where products_name = ?";
            result.add((Db.use(bazzar_ds).query(sqlMax,product_name)).get(0).get("MAX(HighestSellOderPrice)"));
            return result;
        } catch (Exception e) {
            log.error("数据统计失败");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对物品进行统计，并将统计好的数据以List集合方式返回
     * 以日为单位进行统计
     *
     * @param product_name 传入物品name
     * @param TB_NAME      需要进行统计的表格
     * @return 包含所有统计数据的集合
     */
    public static List<Object> statisticsData_day(String product_name, String TB_NAME) {
        List<Object> result = new ArrayList<>();
        String [] colum_list = {"buyPriceAvg","sellPriceAvg","buyVolumeAvg",
                "sellVolumeAvg","LowestBuyOderPriceAvg","HighestSellOderPriceAvg"};
        int limit = 0;
        if(TB_NAME.equals(TB_WK)){
            limit = statistics_week;
        }
        if(TB_NAME.equals(TB_MO)){
            limit = statistics_month;
        }
        try {
            for (String colum_name: colum_list) {
                String sql = "SELECT AVG("+colum_name+") FROM "+ TB_DAY +" where products_name = ? order by id desc limit 0,"+limit;
                List<Entity> entitys = Db.use(bazzar_ds).query(sql,product_name);
                result.add(entitys.get(0).get("AVG("+colum_name+")"));
            }
            String sqlMin = "SELECT Min(LowestBuyOderPrice) FROM "+ TB_DAY +" where products_name = ?";
            result.add((Db.use(bazzar_ds).query(sqlMin,product_name)).get(0).get("Min(LowestBuyOderPrice)"));
            String sqlMax = "SELECT MAX(HighestSellOderPrice) FROM "+ TB_DAY +" where products_name = ?";
            result.add((Db.use(bazzar_ds).query(sqlMax,product_name)).get(0).get("MAX(HighestSellOderPrice)"));
            return result;
        } catch (SQLException e) {
            log.error("日统计数据错误");
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