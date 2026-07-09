package com.platform.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论情感分析总结实体
 * 对应表: sentiment_summary
 * 对应需求: 商品详情页面（客户评论情感分析模块 - 智能总结）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sentiment_summary")
public class SentimentSummary extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long productId;

    /** AI情感分析总结内容 */
    private String summaryText;

    /** 正面评论数 */
    private Integer positiveCount;

    /** 中性评论数 */
    private Integer neutralCount;

    /** 负面评论数 */
    private Integer negativeCount;

    /** 是否推荐购买: 0-不推荐 1-推荐 */
    private Integer recommend;
}
