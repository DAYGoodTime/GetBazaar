package com.day.getbazzarspring.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.day.getbazzarspring.servicesImpl.APIServices;
import com.day.getbazzarspring.servicesImpl.DataServices;
import com.day.getbazzarspring.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.charset.StandardCharsets;

@RestController
public class testController {

    //DEV模式,用离线的JSON文件
    private static final JSONObject offlineJSON = JSONUtil.readJSONObject(new File("D:\\DEV\\java\\GetBazzarSpring\\sample.json"), StandardCharsets.UTF_8);

    @Autowired
    DataServices dataServices;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/getJSON")
    public R getJson2() {
        dataServices.processJSON(offlineJSON);
        return R.OK();
    }

    @GetMapping("/name")
    public R name() {

        return R.OK();
    }
}
