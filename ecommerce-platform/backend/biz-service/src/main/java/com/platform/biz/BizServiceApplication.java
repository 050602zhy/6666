package com.platform.biz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 业务服务启动类
 */
@EnableFeignClients(basePackages = "com.platform.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class BizServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BizServiceApplication.class, args);
    }
}
