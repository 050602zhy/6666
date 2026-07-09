package com.platform.ai.service;

/**
 * 智能推荐服务接口
 */
public interface RecommendService {

    /**
     * 为用户推荐商品
     * @param userId 用户ID
     * @return 推荐结果
     */
    String recommend(Long userId);
}
