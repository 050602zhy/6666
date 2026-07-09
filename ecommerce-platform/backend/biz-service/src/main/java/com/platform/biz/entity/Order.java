package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 * 对应表: orders
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Order extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 订单编号 */
    private Integer orderNo;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品主图 */
    private String productImage;

    /** 购买数量 */
    private Integer quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** VIP折扣金额 */
    private BigDecimal vipDiscount;

    /** 促销活动ID */
    private Long activityDiscountId;

    /** 活动折扣率 */
    private BigDecimal discountRate;

    /** 买家ID */
    private Long buyerId;

    /** 卖家ID */
    private Long sellerId;

    /** 订单状态: 0-待付款 1-已付款待发货 2-已发货 3-已完成 4-已取消 */
    private Integer status;

    /** 是否已评价: 0-未评价 1-已评价 */
    private Integer isRated;

    /** 评价过期时间 */
    private LocalDateTime rateExpireTime;

    /** 退款状态: 0-无 1-退款中 2-退款成功 3-退款拒绝 */
    private Integer refundStatus;
}
