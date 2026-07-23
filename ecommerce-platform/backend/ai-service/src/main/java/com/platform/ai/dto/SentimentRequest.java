package com.platform.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 情绪分析请求DTO
 */
@Data
@Schema(description = "情绪分析请求")
public class SentimentRequest {

    @Schema(description = "待分析的文本内容", example = "你们的工作让我不太满意")
    private String text;
}
