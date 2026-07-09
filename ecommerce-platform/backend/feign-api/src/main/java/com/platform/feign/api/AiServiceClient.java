package com.platform.feign.api;

import com.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * AI 服务 Feign 客户端
 */
@FeignClient(name = "ai-service")
public interface AiServiceClient {

    /**
     * 智能推荐
     */
    @GetMapping("/ai/recommend")
    Result<String> recommend(@RequestParam("userId") Long userId);
}
