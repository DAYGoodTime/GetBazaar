package com.day.getbazzarspring;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.day.getbazzarspring.config.BazzarConfig;
import com.day.getbazzarspring.dao.RedisDao;
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
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    RedisDao redisDao;
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
//            redisTemplate.opsForList().range()
//        }
        System.out.println(redisTemplate.opsForList().range("RAW_FISH:1", 0, -1));
    }

    @Test
    void statisticsData_DAY() {
        JSONObject json = offlineJSON.getJSONObject("products");
        dataServices.statisticsData_DAY(json.keySet());
    }
}
