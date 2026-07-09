package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 活动商品折扣关联实体
 * 对应表: activity_product
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity_product")
public class ActivityProduct extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 活动ID */
    private Long activityId;

    /** 商品ID */
    private Long productId;

    /** 折扣率 */
    private BigDecimal discount;

    /** 折后价格 */
    private BigDecimal discountPrice;

    /** 原价 */
    private BigDecimal originalPrice;
}
