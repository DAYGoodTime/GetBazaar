package com.day.getbazzarspring.controller;

import cn.hutool.json.JSONObject;
import com.day.getbazzarspring.utils.R;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

@RestController
@CrossOrigin
public class MainController {

    @Resource(name = "NormalRedisTemplate")
    RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "StringRedisTemplate")
    RedisTemplate<String, String> stringRedisTemplate;


    @PostMapping("/getQuickState/{name}")
    public R getQuick(@PathVariable(name = "name") String name) {
        String product_name = getName(name);
        if (product_name == null) return R.error("物品不存在");
        JSONObject entries = new JSONObject();
        entries.putAll((Map<String, Object>) (redisTemplate.opsForList().range("quickState:" + product_name, 0, 0).get(0)));
        return R.OK_json(entries);
    }

    @PostMapping("/getSummary/{name}")
    public R getSummary(@PathVariable(name = "name") String name) {
        String product_name = getName(name);
        if (product_name == null) return R.error("物品不存在");
        JSONObject entries = new JSONObject();
        List<Object> mapList = redisTemplate.opsForList().range("product_nm:" + product_name, 0, -1);
        Long length = redisTemplate.opsForList().size("product_nm:" + product_name);
        if (length == null || mapList == null) {
            return R.error("物品不存在");
        }
        List<BigDecimal> LowestBuyOderPriceS = new ArrayList<>(length.intValue());
        List<BigDecimal> HighestSellOderPriceS = new ArrayList<>(length.intValue());
        List<BigDecimal> buyPriceS = new ArrayList<>(length.intValue());
        List<BigDecimal> sellPriceS = new ArrayList<>(length.intValue());
        List<Long> timeStamp = new ArrayList<>(length.intValue());
        for (Object o : mapList) {
            buyPriceS.add(((Map<String, BigDecimal>) o).get("buyPrice"));
            sellPriceS.add(((Map<String, BigDecimal>) o).get("buyPrice"));
            HighestSellOderPriceS.add(((Map<String, BigDecimal>) o).get("buyPrice"));
            LowestBuyOderPriceS.add(((Map<String, BigDecimal>) o).get("LowestBuyOderPrice"));
            timeStamp.add(((Map<String, Long>) o).get("timestamp"));
        }
        entries.set("Summary_count", length);
        entries.set("buySummary", buyPriceS);
        entries.set("sellSummary", sellPriceS);
        entries.set("LowestBuyOrderSummary", LowestBuyOderPriceS);
        entries.set("HighestSellOderSummary", HighestSellOderPriceS);
        entries.set("timestamp",timeStamp);
        return R.OK_json(entries);
    }


    @GetMapping("/getAllName")
    public R getAllName() {
        JSONObject json = new JSONObject();
        List<Map<String, String>> mapList = new ArrayList<>();
        Set<String> keys = stringRedisTemplate.keys("name:*");
        if (keys == null || keys.isEmpty()) {
            return R.error("列表为空");
        }
        for (String name : keys) {
            Map<String, String> map = new HashMap<>(1);
            String value = stringRedisTemplate.opsForValue().get(name);
            if (value != null) {
                value = value.replaceAll("\"", "");
            }
            map.put(name.replaceAll("name:", ""), value);
            mapList.add(map);
        }
        json.set("mappingList", mapList);
        return R.OK_json(json);
    }

    private String getName(String name) {
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey("name:" + name))) return null;
        return stringRedisTemplate.opsForValue().get("name:" + name).replaceAll("\"", "");
    }

}
