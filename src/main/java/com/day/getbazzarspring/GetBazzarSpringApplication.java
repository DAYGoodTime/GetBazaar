package com.day.getbazzarspring;

import cn.hutool.log.LogFactory;
import cn.hutool.log.dialect.slf4j.Slf4jLogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class GetBazzarSpringApplication {

    public static void main(String[] args) {
        //LogFactory.setCurrentLogFactory(new Slf4jLogFactory());
        SpringApplication.run(GetBazzarSpringApplication.class, args);
    }

}
