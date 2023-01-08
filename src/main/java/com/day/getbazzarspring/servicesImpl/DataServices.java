package com.day.getbazzarspring.servicesImpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.id.NanoId;
import cn.hutool.json.JSONObject;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.day.getbazzarspring.dao.ProductDAYMapper;
import com.day.getbazzarspring.dto.FullProduct;
import com.day.getbazzarspring.pojo.ProductDAY;
import com.day.getbazzarspring.pojo.ProductNM;
import com.day.getbazzarspring.pojo.QuickState;
import com.day.getbazzarspring.utils.InitProcess;
import com.day.getbazzarspring.utils.ListUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@Service
public class DataServices {
    public static final boolean isDEV = false;
    private static final Log log = LogFactory.get();

    @Resource(name = "NormalRedisTemplate")
    RedisTemplate<String, Object> redisTemplate;

    private static final int DAYCount = 1440;

    @Autowired
    SQLServices sqlServices;
    private Long lastTime;

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
    public static String formatItemName(String products_name) {
        if (products_name.contains(":")) {
            return products_name.replace(":", "-");
        } else return products_name;
    }

    public void processJSON(JSONObject fullJSON) {
        Long timestamp = fullJSON.getLong("lastUpdated");
        if (lastTime != null && lastTime.equals(timestamp)) {
            return;
        } else {
            lastTime = timestamp;
        }
        Long times = InitProcess.times;
        if (times == null) {
            times = 1L;
        }
        JSONObject products = fullJSON.getJSONObject("products");
        Set<String> products_name = products.keySet();

        if (times % DAYCount == 0 && times != 0) {
            log.info("执行日统计操作");
            statisticsData_DAY(products_name);
        }
        if (Boolean.FALSE.equals(redisTemplate.hasKey("name:" + "ABSOLUTE_ENDER_PEARL"))) {
            initName(products_name);
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
                if (Boolean.TRUE.equals(redisTemplate.hasKey("quickState:" + product_name)))
                    redisTemplate.opsForList().rightPop("quickState:" + product_name);
                redisTemplate.opsForList().leftPush("quickState:" + product_name, BeanUtil.beanToMap(quickState));
                redisTemplate.opsForList().leftPush("product_nm:" + product_name, BeanUtil.beanToMap(ProductNM.build(quickState)));
            } catch (Throwable t) {
                log.error("{}物品发生异常{}", product_name, t.getMessage());
            }
        }
        InitProcess.times++;
    }

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
                String uni_id = NanoId.randomNanoId(30);
                ProductDAY productDAY = new ProductDAY(uni_id, name,
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

    private void initName(Set<String> products_name) {
        for (String name : products_name) {
            redisTemplate.opsForValue().set("name:" + formatItemName(name), name);
        }
    }
}
