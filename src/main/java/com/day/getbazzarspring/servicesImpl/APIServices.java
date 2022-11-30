package com.day.getbazzarspring.servicesImpl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.day.getbazzarspring.config.BazzarConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class APIServices {
    private static final Log log = LogFactory.get();
    public static Long times = 1L;
    @Autowired
    BazzarConfig bzcfg;
    @Autowired
    DataServices dataServices;

    /**
     * 从Hpyixel skyblock api当中获取Bazzar的数据，并将数据作为JSON对象返回
     *
     * @return hutool的JSONObject：BazzarAPI获得的全部数据
     */
    private JSONObject getBazzarJSON() {
        //接受输入
        String jsonString = ConnectAPI().toString();
        while (jsonString.isEmpty()) {
            log.warn("JSON字符串数据为空，正在尝试重新获取");
            jsonString = ConnectAPI().toString();
        }
        JSONObject json = JSONUtil.parseObj(jsonString);
        while (!json.getBool("success")) {
            log.warn("Bazzar返回数据失败，正在尝试重新获取");
            json = JSONUtil.parseObj(ConnectAPI());
        }
        return json;
        //return createFile.createJsonFile(json,"E:\\modding\\SmallProject\\GetBazzar\\Bazzar.json");
    }

    /**
     * 内部方法,使用递归方法进行重连
     *
     * @return 链接成功后返回JSON字符串
     */
    private StringBuilder ConnectAPI() {
        StringBuilder stringBuilder = null;
        try {
            stringBuilder = new StringBuilder(HttpUtil.get(bzcfg.SB_BAZZAR_API));
        } catch (Throwable e) {
            if (bzcfg.reConnectCount == bzcfg.maxReConnectCount) {
                ;
                log.error("重连达到最大次数! \n {}", e);
            } else {
                log.warn("连接失败:{},{}秒开始重连,重连次数:{}", e.getMessage(), bzcfg.reConnectTime, bzcfg.reConnectCount);
                try {
                    Thread.sleep(bzcfg.reConnectTime * 1000L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                bzcfg.reConnectCount++;
                return ConnectAPI();
            }
        }
        bzcfg.reConnectCount = 1;
        return stringBuilder;
    }

    @Scheduled(cron = "0 * * * * ? ")
    public void TimeTask() {
        log.info("第{}次获取API数据", times++);
        try {
            dataServices.processJSON(getBazzarJSON());
        } catch (Throwable t) {
            log.error(t);
        }
    }

}
