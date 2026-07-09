package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 售后退款实体
 * 对应表: refund
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("refund")
public class Refund extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 买家ID */
    private Long buyerId;

    /** 卖家ID */
    private Long sellerId;

    /** 退款原因 */
    private String reason;

    /** 退款状态: 0-待处理 1-同意退款 2-拒绝退款 */
    private Integer status;

    /** 卖家回复 */
    private String reply;
}
