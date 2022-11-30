package com.day.getbazzarspring.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedisDao {
    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    public Map<String, Object> getMap(String key, String hashKey) {
        return (Map<String, Object>) redisTemplate.opsForHash().get(key, hashKey);
    }

    public void pushNameList(String id, String name) {
        redisTemplate.opsForList().leftPush(id, name);
    }

}
