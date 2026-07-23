package com.platform.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI 智能服务启动类
 */
@EnableFeignClients(basePackages = "com.platform.feign")
@EnableDiscoveryClient
@EnableAsync
@MapperScan("com.platform.ai.mapper")
@SpringBootApplication(scanBasePackages = {"com.platform.ai", "com.platform.common"})
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
