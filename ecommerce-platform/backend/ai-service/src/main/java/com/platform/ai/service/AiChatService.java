package com.platform.ai.service;

import com.platform.ai.vo.ChatResponseVO;

import java.util.List;

/**
 * AI 聊天服务
 */
public interface AiChatService {

    /**
     * 智能客服：回答商品相关问题
     *
     * @param productName 商品名称
     * @param productDesc 商品描述
     * @param question    用户问题
     * @return AI 回复
     */
    ChatResponseVO chatAboutProduct(String productName, String productDesc, String question);

    /**
     * 评论情感分析：分析商品评论并给出购买建议
     *
     * @param productName 商品名称
     * @param comments    评论列表
     * @return 分析总结
     */
    ChatResponseVO analyzeComments(String productName, List<String> comments);

    /**
     * 营销文案生成：根据活动描述生成活动方案
     *
     * @param description 用户描述（目的、面向群体、商品、利润等）
     * @return 生成的活动方案
     */
    ChatResponseVO generateMarketingCopy(String description);
}