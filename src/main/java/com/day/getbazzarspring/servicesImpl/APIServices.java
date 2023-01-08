package com.day.getbazzarspring.servicesImpl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.day.getbazzarspring.config.BazaarConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class APIServices {
    private static final Log log = LogFactory.get();
    @Autowired
    DataServices dataServices;
    @Autowired
    BazaarConfig bzcfg;

    @Scheduled(cron = "0 * * * * ? ")
    public void TimeTask() {
        JSONObject json = getJSON();
        if (json==null|| json.isEmpty()) return;
        dataServices.processJSON(json);
    }

    /**
     * 内部方法,用于循环请求数据
     * 如果请求失败就返回空数据
     *
     * @return 数据的JSON字符串, 但是可能为NULL
     */
    private StringBuilder ConnectAPI() throws RuntimeException{
        StringBuilder stringBuilder = null;
        try {
            stringBuilder = new StringBuilder(HttpUtil.get(bzcfg.SB_BAZAAR_API));
        }catch (Throwable t){
            log.warn("api数据获取异常:{},{}秒后重试",t.getMessage(),bzcfg.reConnectTime);
        }
        return stringBuilder;
    }

    /**
     * 从Hpyixel skyblock api当中获取Bazzar的数据，并将数据作为JSON对象返回
     *
     * @return hutool的JSONObject：BazzarAPI获得的全部数据
     */
    public JSONObject getJSON(){
        int reC = 1;
        //接受输入
        StringBuilder jsonString = ConnectAPI();
        while (jsonString==null||jsonString.toString().isEmpty()&&reC< bzcfg.retryCounts){
            try {
                Thread.sleep(1000L*bzcfg.reConnectTime);
            } catch (InterruptedException e) {
                log.error("线程终止异常",e);
            }
            jsonString = ConnectAPI();
        }

        return JSONUtil.parseObj(jsonString.toString());
        //return createFile.createJsonFile(json,"E:\\modding\\SmallProject\\GetBazzar\\Bazzar.json");
    }
}
