package com.day.getBazzar;

import java.util.Timer;
import java.util.TimerTask;

import cn.hutool.http.HttpUtil;
import cn.hutool.log.*;
import com.alibaba.fastjson.*;

import static com.day.getBazzar.GlobalVar.*;

/**
 * 用于获取BazzarJSON数据
 */
public class GetBazzar {

    public static int times=0;
    //连接API进行重连次数
    static int  reConnectCount = 1;
    static int maxReConnectCount = 10;
    private static final Log log = LogFactory.get();
    /**
     * 用于反复执行更新数据操作的timerTask
     */
    static class continuedGet extends TimerTask{
        @Override
        public void run() {
            try {
                JSONObject SB_BAZZAR_JSON = getBazzarJSON();
                log.info("第{}次获取 API时间:{} id_row_day={} id_row_nm={} times={}",
                        (times+1),ToolClass.timestampToDate((Long) SB_BAZZAR_JSON.get("lastUpdated")),
                        BazzarData.id_row_day,BazzarData.id_row_nm,times);
                BazzarData.KeepUpdateBazzarData_quick(SB_BAZZAR_JSON);
            } catch (Throwable e) {
                log.error("A {} Exception",e);
                e.printStackTrace();
            }
            if(times == TotalTimes-1){
                cancel();
            } else
                times++;
        }
    }

    //拟运行主类
    public static void main(String[] args) {
        //读入参数，这里创建对象是因为静态方法会导致线程一直运行
        GlobalVar gv = new GlobalVar();
        gv.readConfig();
        //这里用于输入上次运行的状态
        if(args.length == 2){
            times = Integer.parseInt(args[0]);
            BazzarData.id_row_nm = Integer.parseInt(args[1]);
        }
        if(args.length == 3){
            if(args[2].equalsIgnoreCase("Y")){
                BazzarData.InitializedDBandTable();
            }
        }
        Timer timer = new Timer();
        timer.schedule(new continuedGet(), 100, API_Time * 1000L);  //0.1秒后执行，并且每隔1分钟重复执行
        //这里不是循环结束后运行的代码，循环任务会分开成一个子线程运行。
    }

    /**
     * 从Hpyixel skyblock api当中获取Bazzar的数据，并将数据作为JSON对象返回
     * @return fastjson的JSONObject：Bazzar的全部数据
     */
    public static JSONObject getBazzarJSON() {
        //接受输入
        String jsonString = ConnectAPI().toString();
        JSONObject json = JSONObject.parseObject(jsonString);
        if(json==null){
            log.error("API数据为空!");
        }
        return json;
        //return createFile.createJsonFile(json,"E:\\modding\\SmallProject\\GetBazzar\\Bazzar.json");
    }

    /**
     * 内部方法,使用递归方法进行重连
     * @return 链接成功后返回JSON字符串
     */
    private static StringBuilder ConnectAPI()  {
        StringBuilder stringBuilder = null;
        try{
            stringBuilder = new StringBuilder(HttpUtil.get(SB_BAZZAR_API));
        } catch (Throwable e){
            if(reConnectCount == maxReConnectCount){
                System.out.println("重连达到最大次数");
                log.error("重连达到最大次数! \n {}",e);
            }else {
                log.warn("连接失败,{}秒开始重连,重连次数:{}",reConnectTime,reConnectCount);
                try {
                    Thread.sleep(reConnectTime*1000L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                reConnectCount++;
                return ConnectAPI();
            }
        }
        reConnectCount = 1;
        return stringBuilder;
    }
}