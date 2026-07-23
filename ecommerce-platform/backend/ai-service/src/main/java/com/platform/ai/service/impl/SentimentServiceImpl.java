package com.platform.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.ai.service.SentimentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 情绪分析服务实现
 * 调用 LLM API 分析文本情绪
 */
@Slf4j
@Service
public class SentimentServiceImpl implements SentimentService {

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${ai.endpoint-id:}")
    private String endpointId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 判断是否配置了真实 API Key */
    private boolean hasRealApiKey() {
        return apiKey != null && !apiKey.isEmpty()
                && !apiKey.equals("sk-placeholder")
                && !apiKey.equals("dummy");
    }

    private String getModel() {
        if (endpointId != null && !endpointId.isEmpty()) {
            return endpointId;
        }
        return "deepseek-chat";
    }

    private String getApiUrl() {
        if (baseUrl.endsWith("/")) {
            return baseUrl + "chat/completions";
        }
        return baseUrl + "/chat/completions";
    }

    @Override
    public int analyze(String text) {
        if (text == null || text.isBlank()) {
            return 2;
        }

        // 未配置 API Key，返回模拟结果
        if (!hasRealApiKey()) {
            log.warn("未配置AI API Key，情绪分析返回模拟结果");
            return mockAnalyze(text);
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", getModel());
            requestBody.put("temperature", 0.3);

            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content",
                            "你是一个情绪分析专家。请分析下面这句话的客户情绪，只回复一个数字：1表示正面，2表示中性，3表示负面。不要输出任何其他内容。"),
                    Map.of("role", "user", "content", text)
            );
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(getApiUrl(), entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String reply = root.path("choices").get(0).path("message").path("content").asText("");
            String trimmed = reply.trim();

            log.info("情绪分析 | text={} | raw={}", text, trimmed);

            if (trimmed.contains("1") || trimmed.contains("正面")) {
                return 1;
            } else if (trimmed.contains("3") || trimmed.contains("负面")) {
                return 3;
            }
            return 2;
        } catch (Exception e) {
            log.error("情绪分析失败", e);
            return 2;
        }
    }

    /**
     * 模拟情绪分析（未配置API时使用）
     */
    private int mockAnalyze(String text) {
        // 负面关键词
        String[] negativeWords = {"差", "糟糕", "失望", "垃圾", "烂", "不好", "不行", "不满意",
                "讨厌", "恶心", "烦", "气", "怒", "太差", "很烂", "后悔", "差评"};
        // 正面关键词
        String[] positiveWords = {"满意", "好", "棒", "喜欢", "赞", "不错", "完美", "优秀",
                "推荐", "开心", "高兴", "好评", "值得", "给力", "优秀"};

        int negCount = 0, posCount = 0;
        for (String w : negativeWords) {
            if (text.contains(w)) negCount++;
        }
        for (String w : positiveWords) {
            if (text.contains(w)) posCount++;
        }

        if (negCount > posCount) return 3;
        if (posCount > negCount) return 1;
        return 2;
    }
}
