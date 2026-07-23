package com.platform.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * AI 营销文案生成响应（结构化）
 * 用于活动文案一键填充表单
 */
@Data
public class MarketingCopyVO {

    /** 活动名称 */
    private String name;

    /** 活动描述 */
    private String description;

    /** 改进建议列表 */
    private List<String> suggestions;

    /** 原始 AI 回复（解析失败时可用于展示） */
    private String rawContent;

    /** 是否模拟回复（未配置 API Key 时） */
    private Boolean simulated = false;
}
