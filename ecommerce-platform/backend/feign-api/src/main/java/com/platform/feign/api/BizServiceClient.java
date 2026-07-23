package com.platform.feign.api;

import com.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

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

    /**
     * 获取用户信息
     * @param id 用户ID
     * @return 用户信息（含偏好、个性化推荐设置）
     */
    @GetMapping("/biz/user/{id}")
    Result<Map<String, Object>> getUserById(@PathVariable("id") Long id);

    /**
     * 获取所有上架商品列表
     * @return 上架商品列表
     */
    @GetMapping("/biz/product/onSale/list")
    Result<List<Map<String, Object>>> listOnSaleProducts();

    /**
     * 获取当前进行中活动的所有商品
     * @return 活动商品列表（含折扣信息）
     */
    @GetMapping("/biz/activity/active/products")
    Result<List<Map<String, Object>>> listActiveActivityProducts();

    /**
     * 获取所有商品分类
     * @return 分类列表
     */
    @GetMapping("/biz/category/list")
    Result<List<Map<String, Object>>> listCategories();
}
