package com.day.getbazzarspring.task;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.day.getbazzarspring.config.BazzarConfig;
import com.day.getbazzarspring.servicesImpl.DataServices;

import java.util.concurrent.Callable;

public class APITask implements Callable<Void> {


    private static final Log log = LogFactory.get();
    private BazzarConfig bzcfg;
    private DataServices dataServices;

    public APITask(BazzarConfig bzcfg, DataServices dataServices) {
        this.bzcfg = bzcfg;
        this.dataServices = dataServices;
    }

    /**
     * 内部方法,用于循环请求数据
     * 如果请求失败就返回空数据
     *
     * @return 数据的JSON字符串, 但是可能为NULL
     */
    private StringBuilder ConnectAPI() {
        StringBuilder stringBuilder = null;
        try {
            stringBuilder = new StringBuilder(HttpUtil.get(bzcfg.SB_BAZZAR_API));
        } catch (Throwable e) {
            log.warn("连接失败:{},{}秒开始重连", e.getMessage(), bzcfg.reConnectTime);
            try {
                Thread.sleep(bzcfg.reConnectTime * 1000L);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return ConnectAPI();
        }
        return stringBuilder;
    }

    /**
     * 从Hpyixel skyblock api当中获取Bazzar的数据，并将数据作为JSON对象返回
     *
     * @return hutool的JSONObject：BazzarAPI获得的全部数据
     */
    public JSONObject getJSON() throws InterruptedException {
        //接受输入
        StringBuilder jsonString = ConnectAPI();
        while (jsonString == null) {
            log.warn("获取数据失败,{}秒后重新获取", bzcfg.reConnectTime);
            Thread.sleep(bzcfg.reConnectTime * 1000L);
            jsonString = ConnectAPI();
        }
        return JSONUtil.parseObj(jsonString.toString());
        //return createFile.createJsonFile(json,"E:\\modding\\SmallProject\\GetBazzar\\Bazzar.json");
    }

    @Override
    public Void call() throws Exception {
        dataServices.processJSON(getJSON());
        return null;
    }
}
