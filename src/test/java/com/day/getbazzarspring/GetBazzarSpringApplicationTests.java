package com.day.getbazzarspring;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.day.getbazzarspring.config.BazzarConfig;
import com.day.getbazzarspring.pojo.ProductDAY;
import com.day.getbazzarspring.pojo.ProductNM;
import com.day.getbazzarspring.servicesImpl.APIServices;
import com.day.getbazzarspring.servicesImpl.DataServices;
import com.day.getbazzarspring.servicesImpl.SQLServices;
import com.day.getbazzarspring.utils.ListUtil;
import com.day.getbazzarspring.utils.TimeUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootTest
class GetBazzarSpringApplicationTests {

    //DEV模式,用离线的JSON文件
    private static final JSONObject offlineJSON = JSONUtil.readJSONObject(new File("D:\\DEV\\java\\GetBazzarSpring\\sample.json"), StandardCharsets.UTF_8);
    private static final Log log = LogFactory.get();
    @Autowired
    APIServices apiServices;
    @Resource(name = "NormalRedisTemplate")
    RedisTemplate<String, Object> redisTemplate;

    @Resource(name = "StringRedisTemplate")
    RedisTemplate<String, String> stringRedisTemplate;

    @Autowired
    BazzarConfig bzcfg;

    @Autowired
    DataServices dataServices;
    @Autowired
    SQLServices sqlServices;

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

    @Test
    void contextLoads() {
        dataServices.processJSON(offlineJSON);
    }

    @Test
    void list() {
//        JSONObject products = offlineJSON.getJSONObject("products");
//        Set<String> names = products.keySet();
//        for (String name : names) {
//            redisTemplate.opsForValue().set("name:"+formatItemName(name),name);
//        }
        String s = stringRedisTemplate.opsForValue().get("name:ABSOLUTE_ENDER_PEARL");
        System.out.println(s);
    }

    @Test
    void get() {
        List<Object> range = redisTemplate.opsForList().range("quickState:ABSOLUTE_ENDER_PEARL", 0, 0);
        Object o = range.get(0);
        System.out.println(o);
    }

    @Test
    void statisticsData_DAY() {
        JSONObject json = offlineJSON.getJSONObject("products");
        dataServices.statisticsData_DAY(json.keySet());
    }
}
