package com.platform.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动状态枚举
 */
@Getter
@AllArgsConstructor
public enum ActivityStatus implements BaseEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    ENDED(2, "已结束");

    private final int code;
    private final String desc;
}
