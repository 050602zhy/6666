package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 营销活动实体
 * 对应表: activity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activity")
public class Activity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 活动名称 */
    private String name;

    /** 活动描述 */
    private String description;

    /** 活动状态: 0-草稿 1-已发布 2-已结束 */
    private Integer status;

    /** 创建人ID */
    private Long creatorId;

    /** 活动开始时间 */
    private LocalDateTime startTime;

    /** 活动结束时间 */
    private LocalDateTime endTime;
}
