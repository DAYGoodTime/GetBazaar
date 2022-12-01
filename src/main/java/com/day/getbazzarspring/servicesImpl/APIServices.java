package com.day.getbazzarspring.servicesImpl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.day.getbazzarspring.config.BazzarConfig;
import com.day.getbazzarspring.task.APITask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class APIServices {
    private static final Log log = LogFactory.get();
    public static Long times = 1L;
    @Autowired
    BazzarConfig bzcfg;
    @Autowired
    DataServices dataServices;


    private Future<Void> task;

    private ExecutorService executorService = Executors.newFixedThreadPool(1);

    //@Scheduled(cron = "0 * * * * ? ")
    public void TimeTask() throws ExecutionException, InterruptedException {
        log.info("第{}次获取API数据", times++);
        if (task == null) {
            task = executorService.submit(new APITask(bzcfg, dataServices));
            return;
        }
        if (!task.isDone()) {
            log.warn("获取数据超时,重新获取");
            BazzarConfig.Keep = false;
        }
        task.get();
        BazzarConfig.Keep = true;
        task = executorService.submit(new APITask(bzcfg, dataServices));
    }
}
