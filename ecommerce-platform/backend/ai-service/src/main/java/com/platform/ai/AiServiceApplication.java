package com.platform.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI 智能服务启动类
 */
@EnableFeignClients(basePackages = "com.platform.feign")
@EnableDiscoveryClient
// 排除数据源自动配置，ai-service 不连接数据库
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}
