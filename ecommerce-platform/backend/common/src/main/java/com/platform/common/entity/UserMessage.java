package com.platform.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户消息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_message")
@Schema(description = "用户消息实体")
public class UserMessage extends BaseEntity {

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "消息类型：1-退货申请，2-退货答复，3-折扣活动，4-订单通知")
    @TableField("type")
    private Integer type;

    @Schema(description = "消息标题")
    @TableField("title")
    private String title;

    @Schema(description = "消息内容")
    @TableField("content")
    private String content;

    @Schema(description = "是否已读：0-未读，1-已读")
    @TableField("is_read")
    private Integer isRead;

    @Schema(description = "关联ID")
    @TableField("related_id")
    private Long relatedId;
}
