package com.dayouzc.efk.logcenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author FanJiangFeng
 * @version 1.0.0
 * @ClassName ClientPoolConfig.java
 * @Description TODO
 * @createTime 2021年06月08日 18:09:00
 */
@Configuration
public class ClientPoolConfig {

    @Bean
    public ClientPool clientPool(){
        return new ClientPool();
    }
}
