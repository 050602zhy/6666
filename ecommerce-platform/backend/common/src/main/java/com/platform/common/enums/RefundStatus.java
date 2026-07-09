package com.platform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 退款状态枚举
 */
@Getter
@AllArgsConstructor
public enum RefundStatus implements BaseEnum {

    NO_REFUND(0, "无退款"),
    REFUNDING(1, "退款中"),
    REFUND_SUCCESS(2, "退款成功"),
    REFUND_REJECTED(3, "退款拒绝");

    private final int code;
    private final String desc;
}
