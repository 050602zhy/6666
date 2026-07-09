package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品评论实体
 * 对应表: product_review
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_review")
public class ProductReview extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long productId;

    /** 用户ID */
    private Long userId;

    /** 订单ID */
    private Long orderId;

    /** 评论内容 */
    private String content;

    /** 评分: 1~5 */
    private Integer rating;

    /** 情感分析结果: 1-正面 0-中性 -1-负面 */
    private Integer sentiment;
}
