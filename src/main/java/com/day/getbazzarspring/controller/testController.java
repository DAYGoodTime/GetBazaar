package com.day.getbazzarspring.controller;

import cn.hutool.json.JSONObject;
import com.day.getbazzarspring.servicesImpl.DataServices;
import com.day.getbazzarspring.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@RestController
public class testController {

    //DEV模式,用离线的JSON文件
    //private static final JSONObject offlineJSON = JSONUtil.readJSONObject(new File("D:\\DEV\\java\\GetBazzarSpring\\sample.json"), StandardCharsets.UTF_8);

    @Autowired
    DataServices dataServices;

    @Autowired
    RedisTemplate<String, String> stringRedisTemplate;

    @GetMapping("/debug/initName")
    public String hello(HttpServletRequest request) {
        boolean flag = false;
        Cookie[] cookies = request.getCookies();
        for (Cookie ck : cookies) {
            flag = ck.getName().equals("person") && ck.getValue().equals("day");
            if (flag) break;
        }
        if (!flag) {
            return "DEFINE";
        }
        return "OK";
    }

    @GetMapping("/test/addName")
    public R addName(@RequestBody JSONObject json) {
        String id = json.getStr("id");
        String name = json.getStr("name");
        if (id == null || id.isEmpty()) return R.error("id is Null or Empty");
        if (name == null || name.isEmpty()) return R.error("name is Null or Empty");
        stringRedisTemplate.opsForValue().set("name:" + name, id);
        return R.OK();
    }

}
