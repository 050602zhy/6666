package com.platform.ai.service;

import com.platform.ai.vo.RecommendProductVO;

import java.util.List;

/**
 * 智能推荐服务接口
 */
public interface RecommendService {

    /**
     * 为用户推荐商品
     * @param userId 用户ID
     * @return 推荐商品列表
     */
    List<RecommendProductVO> recommend(Long userId);
}
