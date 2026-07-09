package com.platform.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI营销文案生成记录实体
 * 对应表: ai_marketing
 * 对应需求: 活动设置页面（自动生成营销文案和活动策划建议模块）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_marketing")
public class AiMarketing extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID(卖家) */
    private Long userId;

    /** 用户输入的活动需求描述 */
    private String userRequirement;

    /** AI生成的活动名称 */
    private String generatedName;

    /** AI生成的活动描述 */
    private String generatedDesc;

    /** AI活动策划建议 */
    private String suggestion;
}
