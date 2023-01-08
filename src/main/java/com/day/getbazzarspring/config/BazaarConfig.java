package com.day.getbazzarspring.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@Configuration
public class BazaarConfig {

    @Value("${bazaar.api.host:https://api.hypixel.net/skyblock/bazaar}")
    public String SB_BAZAAR_API;

    @Min(1)
    @Max(10)
    @Value("${bazaar.api.retryTime:10}")
    public int reConnectTime;
    @Min(1)
    @Max(5)
    @Value("${bazaar.api.retryCount:3}")
    public int retryCounts;

}
