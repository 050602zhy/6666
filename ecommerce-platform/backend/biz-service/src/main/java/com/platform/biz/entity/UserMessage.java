package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户消息实体
 * 对应表: user_message
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_message")
public class UserMessage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 接收用户ID */
    private Long userId;

    /** 发送者ID */
    private Long senderId;

    /** 消息类型: 1-退货申请 2-退货答复 3-折扣活动 4-订单通知 5-系统消息 */
    private Integer type;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 是否已读: 0-未读 1-已读 */
    private Integer isRead;

    /** 关联业务ID */
    private Long relatedId;
}
