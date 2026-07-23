package com.platform.ai.controller;

import com.platform.ai.dto.SentimentRequest;
import com.platform.ai.service.SentimentService;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 情绪分析控制器
 */
@Tag(name = "情绪分析", description = "AI 客户情绪分析")
@Slf4j
@RestController
@RequestMapping("/ai/sentiment")
public class SentimentController {

    private final SentimentService sentimentService;

    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }

    @Operation(summary = "分析文本情绪", description = "返回 1-正面，2-中性，3-负面")
    @PostMapping("/analyze")
    public Result<Integer> analyze(@RequestBody SentimentRequest request) {
        try {
            if (request == null || request.getText() == null || request.getText().isBlank()) {
                return Result.success(2);
            }
            int sentiment = sentimentService.analyze(request.getText());
            return Result.success(sentiment);
        } catch (Exception e) {
            log.error("情绪分析接口异常", e);
            return Result.success(2);
        }
    }
}
