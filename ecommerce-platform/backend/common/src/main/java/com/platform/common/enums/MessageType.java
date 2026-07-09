package com.platform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
@AllArgsConstructor
public enum MessageType implements BaseEnum {

    REFUND_REQUEST(1, "退货申请"),
    REFUND_REPLY(2, "退货答复"),
    DISCOUNT_ACTIVITY(3, "折扣活动"),
    ORDER_NOTIFICATION(4, "订单通知");

    private final int code;
    private final String desc;
}
