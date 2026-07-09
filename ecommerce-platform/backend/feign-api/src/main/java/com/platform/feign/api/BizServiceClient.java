package com.platform.feign.api;

import com.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 业务服务 Feign 客户端
 */
@FeignClient(name = "biz-service")
public interface BizServiceClient {

    /**
     * 获取商品信息
     */
    @GetMapping("/biz/product/{id}")
    Result<Object> getProductById(@PathVariable("id") Long id);
}
