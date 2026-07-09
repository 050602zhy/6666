package com.platform.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 智能客服对话记录实体
 * 对应表: chat_history
 * 对应需求: 商品详情页面（智能客服模块）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_history")
public class ChatHistory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 关联商品ID(可为空) */
    private Long productId;

    /** 用户问题 */
    private String question;

    /** AI回答 */
    private String answer;
}
