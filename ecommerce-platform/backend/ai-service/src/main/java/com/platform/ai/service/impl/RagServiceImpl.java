package com.platform.ai.service.impl;

import com.platform.ai.service.RagService;
import com.platform.ai.vo.ChatResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 检索增强生成服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final ChromaService chromaService;
    private final RestTemplate restTemplate;

    @Value("${chroma.collection-prefix:ecommerce_kb_}")
    private String collectionPrefix;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.endpoint-id}")
    private String endpointId;

    @Value("${ai.temperature:0.7}")
    private double temperature;

    @Override
    public ChatResponseVO chat(Long kbId, String question) {
        try {
            // 1. 从Chroma检索相关文档
            String collectionName = collectionPrefix + kbId;
            List<ChromaService.RetrievalResult> results = chromaService.query(collectionName, question, 3);

            if (results.isEmpty()) {
                ChatResponseVO vo = new ChatResponseVO();
                vo.setContent("抱歉，知识库中暂无与该问题相关的内容。您可以尝试换个方式提问，或联系卖家获取更多帮助。");
                vo.setSimulated(true);
                return vo;
            }

            // 2. 构建RAG Prompt
            StringBuilder context = new StringBuilder();
            context.append("=== 以下是从知识库中检索到的相关内容 ===\n\n");
            for (int i = 0; i < results.size(); i++) {
                ChromaService.RetrievalResult r = results.get(i);
                context.append("【片段").append(i + 1).append("】（来自：").append(r.getDocName()).append("）\n");
                context.append(r.getContent()).append("\n\n");
            }
            context.append("=== 检索内容结束 ===\n\n");

            String systemPrompt = "你是一个专业的电商AI客服助手。你的回答必须严格基于上面提供的知识库内容。\n" +
                    "规则：\n" +
                    "1. 只使用检索到的内容回答问题，不要编造信息\n" +
                    "2. 如果检索内容不足以回答问题，请诚实告知用户\n" +
                    "3. 回答要简洁、专业、有礼貌\n" +
                    "4. 可以用分点的方式组织回答\n" +
                    "5. 称呼用户为\"亲\"\n\n" + context;

            // 3. 调用AI API
            String reply = callAiApi(systemPrompt, question);

            ChatResponseVO vo = new ChatResponseVO();
            vo.setContent(reply.trim());
            vo.setSimulated(false);
            return vo;

        } catch (Exception e) {
            log.error("RAG问答失败", e);
            ChatResponseVO vo = new ChatResponseVO();
            vo.setContent("抱歉，AI服务暂时不可用，请稍后再试。");
            vo.setSimulated(true);
            return vo;
        }
    }

    private String callAiApi(String systemPrompt, String userPrompt) {
        String url = baseUrl + "/chat/completions";

        Map<String, Object> message1 = new HashMap<>();
        message1.put("role", "system");
        message1.put("content", systemPrompt);

        Map<String, Object> message2 = new HashMap<>();
        message2.put("role", "user");
        message2.put("content", userPrompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", endpointId);
        body.put("messages", List.of(message1, message2));
        body.put("temperature", temperature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        Map responseBody = response.getBody();

        if (responseBody != null && responseBody.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                return (String) message.get("content");
            }
        }

        return "抱歉，无法获取AI回复。";
    }
}