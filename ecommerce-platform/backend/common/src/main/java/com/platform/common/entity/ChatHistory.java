package com.platform.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI客服对话记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_history")
@Schema(description = "AI客服对话记录实体")
public class ChatHistory extends BaseEntity {

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "商品ID")
    @TableField("product_id")
    private Long productId;

    @Schema(description = "用户提问")
    @TableField("question")
    private String question;

    @Schema(description = "AI回答")
    @TableField("answer")
    private String answer;
}
