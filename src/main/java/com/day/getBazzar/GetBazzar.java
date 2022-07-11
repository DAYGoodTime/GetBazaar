package com.day.getBazzar;

import java.util.Timer;
import java.util.TimerTask;

import cn.hutool.http.HttpUtil;
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

    /**
     * 用于反复执行更新数据操作的timerTask
     */
    static class continuedGet extends TimerTask{
        @Override
        public void run() {
            try {
                System.out.println("-------第"+(times+1)+"次获取-------");
                JSONObject SB_BAZZAR_JSON = getBazzarJSON(false);
                System.out.print(" JSON更新时间:"+ToolClass.timestampToDate((Long) SB_BAZZAR_JSON.get("lastUpdated")));
                System.out.print(" times:"+times+" id_row_nm:"+BazzarData.id_row_nm+" id_row_day:"+BazzarData.id_row_day);
                BazzarData.KeepUpdateBazzarData_quick(SB_BAZZAR_JSON);
            } catch (Throwable e) {
                System.out.println("times:"+times);
                System.out.println("id_row_nm:"+BazzarData.id_row_nm);
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
            times = 3788;
            BazzarData.id_row_nm = 905;
        BazzarData.InitializedDBandTable();
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
        System.out.println("正在接受API数据:");
        String jsonString = ConnectAPI().toString();
        JSONObject json = JSONObject.parseObject(jsonString);
        System.out.println("获取完成");
        return json;
        //return createFile.createJsonFile(json,"E:\\modding\\SmallProject\\GetBazzar\\Bazzar.json");
    }

    /**
     * 从Hpyixel skyblock api当中获取Bazzar的数据，并将数据作为JSON对象返回
     * @return fastjson的JSONObject：Bazzar的全部数据
     * @param flag 是否输出提示消息
     */
    public static JSONObject getBazzarJSON(boolean flag) {
        //接受输入
        if(flag){
            System.out.println("正在接受API数据:");
        }
        String jsonString = ConnectAPI().toString();
        JSONObject json = JSONObject.parseObject(jsonString);
        if (flag){
            System.out.println("获取完成");
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
                System.out.println("rec:"+reConnectCount + "mRe:"+maxReConnectCount);
                e.printStackTrace();
            }else {
                System.out.println("连接失败,"+reConnectTime+"秒后尝试重连");
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