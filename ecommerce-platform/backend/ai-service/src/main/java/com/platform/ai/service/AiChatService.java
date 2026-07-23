package com.platform.ai.service;

import com.platform.ai.vo.ChatResponseVO;
import com.platform.ai.vo.MarketingCopyVO;

import java.util.List;

/**
 * AI 聊天服务
 */
public interface AiChatService {

    /**
     * 智能客服：回答商品相关问题（基于前端传入的实时数据）
     *
     * @param productName     商品名称
     * @param productDesc     商品描述
     * @param question        用户问题
     * @param productId       商品ID
     * @param userId          用户ID（可为null）
     * @param userBalance     用户余额（可为null）
     * @param userVipLevel    用户VIP等级（可为null）
     * @param productContext  商品实时数据JSON（可为null）
     * @return AI 回复
     */
    ChatResponseVO chatAboutProduct(String productName, String productDesc, String question,
                                    Long productId, Long userId, Double userBalance, Integer userVipLevel, String productContext);

    /**
     * 评论情感分析：分析商品评论并给出购买建议
     *
     * @param productName 商品名称
     * @param comments    评论列表
     * @return 分析总结
     */
    ChatResponseVO analyzeComments(String productName, List<String> comments);

    /**
     * 营销文案生成：根据活动描述生成活动方案（结构化返回）
     *
     * @param description 用户描述（目的、面向群体、商品、利润等）
     * @return 生成的活动方案（结构化，包含名称、描述、建议）
     */
    MarketingCopyVO generateMarketingCopy(String description);

    /**
     * 商品广场AI咨询：回答关于所有商品的问题（推荐、比较、查询等）
     *
     * @param question       用户问题
     * @param productsJson   商品列表JSON（所有上架商品的简要信息）
     * @param userId         用户ID（可为null）
     * @param userBalance    用户余额（可为null）
     * @param userVipLevel   用户VIP等级（可为null）
     * @return AI 回复
     */
    ChatResponseVO chatAboutPlaza(String question, String productsJson,
                                   Long userId, Double userBalance, Integer userVipLevel);
}