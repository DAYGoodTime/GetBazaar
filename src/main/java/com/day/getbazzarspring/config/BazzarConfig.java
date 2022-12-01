package com.day.getbazzarspring.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class BazzarConfig {

    public String SB_BAZZAR_API = "https://api.hypixel.net/skyblock/bazaar";

    public int reConnectTime = 10;

    public static boolean Keep = true;

}
