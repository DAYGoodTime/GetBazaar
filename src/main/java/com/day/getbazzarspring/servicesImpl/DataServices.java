package com.day.getbazzarspring.servicesImpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.day.getbazzarspring.dao.ProductDAYMapper;
import com.day.getbazzarspring.dao.RedisDao;
import com.day.getbazzarspring.dto.FullProduct;
import com.day.getbazzarspring.pojo.ProductDAY;
import com.day.getbazzarspring.pojo.ProductNM;
import com.day.getbazzarspring.pojo.QuickState;
import com.day.getbazzarspring.utils.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class DataServices {
    public static final boolean isDEV = false;
    private static final Log log = LogFactory.get();

    private static final int DAYCount = 1440;
    @Autowired
    SQLServices sqlServices;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    RedisDao redisDao;
    @Autowired
    ProductDAYMapper productDAYMapper;

    /**
     * 用于检测物品名字是否含有特殊字符
     * 例如ink_sack:3等含有副id的特殊物品
     * 若存在则进行修改，否则返回原字符串
     *
     * @param products_name 可能存在特殊字符的物品name
     * @return 返回修改后的物品name
     */
    private static String formatItemName(String products_name) {
        if (products_name.contains(":")) {
            return products_name.replace(":", "-");
        } else return products_name;
    }

    public void processJSON(JSONObject fullJSON) {
        if (fullJSON == null || fullJSON.isEmpty()) {
            return;
        }
        Long timestamp = fullJSON.getLong("lastUpdated");
        Long times = redisTemplate.opsForList().size("product_nm");
        if (times == null) {
            times = 1L;
        }
        JSONObject products = fullJSON.getJSONObject("products");
        Set<String> products_name = products.keySet();
        if (times % DAYCount == 0 && times != 0) {
            log.info("执行日统计操作");
            statisticsData_DAY(products_name);
        }
        for (String product_name : products_name) {
            if (isDEV) {
                log.info("处理中:{}", product_name);
            }
            try {
                if (times >= DAYCount) {
                    redisTemplate.opsForList().rightPop("product_nm:" + product_name);
                }
                FullProduct fullProduct = products.get(product_name, FullProduct.class);
                QuickState quickState = QuickState.build(fullProduct, timestamp);
                redisTemplate.opsForList().leftPush("quickState:" + product_name, BeanUtil.beanToMap(quickState));
                redisTemplate.expire("quickState:" + product_name, 1L, TimeUnit.MINUTES);
                redisTemplate.opsForList().leftPush("product_nm:" + product_name, BeanUtil.beanToMap(ProductNM.build(quickState)));
            } catch (Throwable t) {
                log.error("{}物品发生异常{}", product_name, t.getMessage());
            }
        }
    }

    //被废弃的旧设计
//    public void processJSON2(JSONObject fullJSON){
//        if(fullJSON==null||fullJSON.isEmpty()){
//            return;
//        }
//        Long timestamp = fullJSON.getLong("lastUpdated");
//        Long times = redisTemplate.opsForList().size("product_nm");
//        if(times==null){
//            times = 1L;
//        }
//        if(times % DAYCount==0&&times!=0){
//            log.info("执行日统计操作");
//            statisticsData_DAY();
//        }
//        if(times>=DAYCount){
//            redisTemplate.opsForList().rightPop("product_nm");
//        }
//        JSONObject products = fullJSON.getJSONObject("products");
//        Set<String> products_name = products.keySet();
//        Map<String,Object> RMap = new HashMap<>(products.size());
//        for (String product_name:products_name) {
//            if(isDEV){
//                log.info("处理中:{}",product_name);
//            }
//            FullProduct fullProduct = products.get(product_name,FullProduct.class);
//            QuickState quickState = JSONUtil.toBean(fullProduct.getQuick_status(), QuickState.class);
//            JSONObject HOrder = Order.EmptyObj();
//            JSONObject LOrder = Order.EmptyObj();
//            if(!fullProduct.getSell_summary().isEmpty()) HOrder =fullProduct.getSell_summary().get(0);
//            if(!fullProduct.getBuy_summary().isEmpty())  LOrder =fullProduct.getBuy_summary().get(0);
//            Order HighestSellOderPrice = JSONUtil.toBean(HOrder,Order.class);
//            Order LowestBuyOderPrice = JSONUtil.toBean(LOrder,Order.class);
//            ProductNM productNM = ProductNM.build(LowestBuyOderPrice,HighestSellOderPrice,timestamp,quickState);
//            Map<String,Object> productMap = BeanUtil.beanToMap(productNM);
//            RMap.put(product_name,productMap);
//        }
//        redisTemplate.opsForList().leftPush("product_nm",RMap);
//    }

//    public void statisticsData_DAY(){
//        List<Object> bigList =redisTemplate.opsForList().range("product_nm",0,DAYCount);
//
//        if(bigList==null||bigList.isEmpty()){
//            log.error("需要统计的数据列表为空");
//            return;
//        }
//        Map<String,Object> m =(Map<String,Object>) bigList.get(0);
//        Set<String> name_set = m.keySet();
//        for (String name:name_set) {
//            int counts = name_set.size();
//            List<BigDecimal> LowestBuyOderPriceS = new ArrayList<>(counts);
//            List<BigDecimal> HighestSellOderPriceS = new ArrayList<>(counts);
//            List<BigDecimal> buyPriceS = new ArrayList<>(counts);
//            List<BigDecimal> sellPriceS = new ArrayList<>(counts);
//            for (Object obj :bigList) {
//                Map<String,Object> map1 = (Map<String,Object>)obj;
//                Map<String,Object> product = (Map<String,Object>)map1.get(name);
//                buyPriceS.add((BigDecimal)product.get("buyPrice"));
//                break;
//            }
//            long time = System.currentTimeMillis();
//            ProductDAY productDAY = new ProductDAY(formatItemName(name),
//                    ListUtil.getListAvg(LowestBuyOderPriceS),
//                    ListUtil.getListAvg(HighestSellOderPriceS),
//                    ListUtil.getListMin(LowestBuyOderPriceS),
//                    ListUtil.getListMax(HighestSellOderPriceS),
//                    ListUtil.getListAvg(buyPriceS),
//                    ListUtil.getListAvg(sellPriceS),
//                    time);
//            if(!sqlServices.insertDate(productDAY)) log.error("插入到SQL失败,物品名{}",name);
//        }
//
//    }

    public void statisticsData_DAY(Set<String> products_name) {
        for (String name : products_name) {
            try {
                List<Object> obj = redisTemplate.opsForList().range("product_nm:" + name, 0, -1);
                if (obj == null || obj.isEmpty()) {
                    log.error("统计列表为空");
                    return;
                }
                int counts = obj.size();
                List<BigDecimal> LowestBuyOderPriceS = new ArrayList<>(counts);
                List<BigDecimal> HighestSellOderPriceS = new ArrayList<>(counts);
                List<BigDecimal> buyPriceS = new ArrayList<>(counts);
                List<BigDecimal> sellPriceS = new ArrayList<>(counts);
                for (Object o : obj) {
                    Map<String, Object> map = (Map<String, Object>) o;
                    LowestBuyOderPriceS.add((BigDecimal) map.get("LowestBuyOderPrice"));
                    HighestSellOderPriceS.add((BigDecimal) map.get("HighestSellOderPrice"));
                    buyPriceS.add((BigDecimal) map.get("buyPrice"));
                    sellPriceS.add((BigDecimal) map.get("sellPrice"));
                }
                ProductDAY productDAY = new ProductDAY(name,
                        ListUtil.getListAvg(LowestBuyOderPriceS),
                        ListUtil.getListAvg(HighestSellOderPriceS),
                        ListUtil.getListMin(LowestBuyOderPriceS),
                        ListUtil.getListMax(HighestSellOderPriceS),
                        ListUtil.getListAvg(buyPriceS),
                        ListUtil.getListAvg(sellPriceS),
                        System.currentTimeMillis()
                );
                productDAYMapper.insert(productDAY);
            } catch (Throwable t) {
                log.error("统计{}物品出错:{}", name, t.getMessage());
            }
        }
    }
}
