package com.day.getbazzarspring.utils;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class InitProcess implements ApplicationRunner {

    private static final Log log = LogFactory.get();
    public static Long times = null;
    @Autowired
    RedisTemplate<String, String> stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            times = stringRedisTemplate.opsForList().size("product_nm:" + "INK_SACK:3");
        } catch (Throwable t) {
            log.error("初始化错误", t.getMessage());
        }
    }
}
