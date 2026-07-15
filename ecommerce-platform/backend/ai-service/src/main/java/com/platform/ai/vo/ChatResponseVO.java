package com.platform.ai.vo;

import lombok.Data;

/**
 * AI 聊天响应
 */
@Data
public class ChatResponseVO {

    /** AI 回复内容 */
    private String content;

    /** 是否模拟回复（未配置API Key时） */
    private Boolean simulated = false;
}