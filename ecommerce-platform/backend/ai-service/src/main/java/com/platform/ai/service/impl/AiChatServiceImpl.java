package com.platform.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.ai.service.AiChatService;
import com.platform.ai.vo.ChatResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 聊天服务实现
 * 直接通过 HTTP 调用 OpenAI 兼容 API（支持 DeepSeek、火山方舟等）
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${ai.endpoint-id:}")
    private String endpointId;

    @Value("${ai.temperature:0.7}")
    private double temperature;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 判断是否配置了真实 API Key 和接入点 */
    private boolean hasRealApiKey() {
        return apiKey != null && !apiKey.isEmpty()
                && !apiKey.equals("sk-placeholder")
                && !apiKey.equals("dummy")
                && endpointId != null && !endpointId.isEmpty();
    }

    /** 获取模型名称：火山方舟用 endpoint-id，DeepSeek 用 deepseek-chat */
    private String getModel() {
        if (endpointId != null && !endpointId.isEmpty()) {
            return endpointId;
        }
        return "deepseek-chat";
    }

    /** 获取 API 调用地址 */
    private String getApiUrl() {
        if (baseUrl.endsWith("/")) {
            return baseUrl + "chat/completions";
        }
        return baseUrl + "/chat/completions";
    }

    @Override
    public ChatResponseVO chatAboutProduct(String productName, String productDesc, String question) {
        if (!hasRealApiKey()) {
            log.warn("未配置真实 AI API Key 或接入点 ID，使用模拟回复");
            return simulateChatReply(productName, question);
        }

        String systemPrompt = String.format(
            "你是一家电商平台的智能客服，专门回答关于商品「%s」的问题。\n" +
            "商品描述：%s\n\n" +
            "规则：\n" +
            "1. 只回答与该商品相关的问题（如功能、用途、质量、售后等）。\n" +
            "2. 如果用户问的问题与商品无关，请严格回复：\n" +
            "   \"亲，你的问题超出我的范畴了，请换个问题吧\"\n" +
            "3. 用友好、专业的语气回答，称呼用户为\"亲\"，结尾可以加个表情。\n" +
            "4. 回答尽量简洁，不超过150字。",
            productName, productDesc != null ? productDesc : "暂无描述"
        );

        try {
            String reply = callApi(systemPrompt, "用户问题：" + question);
            ChatResponseVO vo = new ChatResponseVO();
            vo.setContent(reply.trim());
            vo.setSimulated(false);
            return vo;
        } catch (Exception e) {
            log.warn("AI 客服调用失败，降级为模拟回复", e);
            return simulateChatReply(productName, question);
        }
    }

    private ChatResponseVO simulateChatReply(String productName, String question) {
        String q = question.toLowerCase();
        String reply;
        if (q.contains("价格") || q.contains("多少钱") || q.contains("贵") || q.contains("便宜")) {
            reply = "亲，这款「" + productName + "」目前享受平台优惠，性价比超高哦~ 您可以点击购买查看最终价格！";
        } else if (q.contains("质量") || q.contains("好不好") || q.contains("怎么样") || q.contains("耐用") || q.contains("优点")) {
            reply = "亲，这款「" + productName + "」经过严格质检，品质有保障，音质清晰、佩戴舒适、续航持久，很多买家都给了好评呢！";
        } else if (q.contains("缺点") || q.contains("不足") || q.contains("问题") || q.contains("差")) {
            reply = "亲，这款「" + productName + "」整体口碑不错，部分用户反馈在某些场景下还有优化空间，建议您结合自身需求综合考虑哦~";
        } else if (q.contains("颜色") || q.contains("尺寸") || q.contains("规格") || q.contains("型号")) {
            reply = "亲，这款「" + productName + "」有多种规格可选，您可以查看商品详情页了解具体的颜色、尺寸信息哦~";
        } else if (q.contains("快递") || q.contains("物流") || q.contains("发货") || q.contains("多久") || q.contains("几天")) {
            reply = "亲，下单后我们会尽快安排发货，一般1-3天内送达，具体时效以物流信息为准~";
        } else if (q.contains("退") || q.contains("换") || q.contains("售后")) {
            reply = "亲，我们支持7天无理由退换货，如有质量问题可随时联系客服处理哦~";
        } else if (q.contains("库存") || q.contains("有货") || q.contains("缺货")) {
            reply = "亲，这款「" + productName + "」目前库存充足，您可以放心下单！";
        } else if (q.contains("你好") || q.contains("在吗") || q.contains("hi") || q.contains("hello")) {
            reply = "亲，您好！我是「" + productName + "」的智能客服，有什么可以帮您的吗？";
        } else {
            reply = "亲，你的问题超出我的范畴了，请换个问题吧";
        }
        ChatResponseVO vo = new ChatResponseVO();
        vo.setContent(reply);
        vo.setSimulated(true);
        return vo;
    }

    @Override
    public ChatResponseVO analyzeComments(String productName, List<String> comments) {
        if (comments == null || comments.isEmpty()) {
            ChatResponseVO vo = new ChatResponseVO();
            vo.setContent("暂无足够评论生成总结");
            vo.setSimulated(true);
            return vo;
        }

        if (!hasRealApiKey()) {
            log.warn("未配置真实 AI API Key 或接入点 ID，使用模拟回复");
            return simulateCommentAnalysis(productName, comments);
        }

        String commentsText = comments.stream()
            .map((String c) -> "- " + c)
            .collect(Collectors.joining("\n"));

        String systemPrompt = "你是一位专业的电商评论分析师。请根据用户提供的商品评论，完成以下分析：\n" +
            "1. 总结大多数客户的情绪倾向（正面/中性/负面）。\n" +
            "2. 提取客户提到的主要优点和缺点（各3条以内）。\n" +
            "3. 给出是否推荐购买该商品的建议，并说明理由。\n" +
            "4. 用中文回答，语气专业、客观。\n" +
            "5. 回答控制在200字以内，分点清晰。";

        String userPrompt = String.format(
            "商品名称：%s\n\n近20天评论（共%d条）：\n%s",
            productName, comments.size(), commentsText
        );

        try {
            String reply = callApi(systemPrompt, userPrompt);
            ChatResponseVO vo = new ChatResponseVO();
            vo.setContent(reply.trim());
            vo.setSimulated(false);
            return vo;
        } catch (Exception e) {
            log.warn("AI 评论分析调用失败，降级为模拟回复", e);
            return simulateCommentAnalysis(productName, comments);
        }
    }

    private ChatResponseVO simulateCommentAnalysis(String productName, List<String> comments) {
        int positive = 0, negative = 0;
        for (String c : comments) {
            String lower = c.toLowerCase();
            if (lower.contains("好") || lower.contains("不错") || lower.contains("满意") || lower.contains("推荐") || lower.contains("喜欢")) {
                positive++;
            } else if (lower.contains("差") || lower.contains("不好") || lower.contains("失望") || lower.contains("后悔") || lower.contains("垃圾")) {
                negative++;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("【评论情感分析总结】\n\n");
        sb.append("1. 情绪倾向：");
        if (positive > negative) {
            sb.append("整体偏正面，多数用户对「").append(productName).append("」表示满意。\n");
        } else if (negative > positive) {
            sb.append("整体偏负面，部分用户反映存在问题。\n");
        } else {
            sb.append("评价褒贬不一，意见较为分散。\n");
        }
        sb.append("2. 购买建议：");
        if (positive > negative) {
            sb.append("推荐购买。该商品口碑较好，值得入手。\n");
        } else if (negative > positive) {
            sb.append("谨慎购买。建议仔细查看商品详情和用户反馈后再决定。\n");
        } else {
            sb.append("建议结合自身需求考虑。可参考具体评价做出选择。\n");
        }

        ChatResponseVO vo = new ChatResponseVO();
        vo.setContent(sb.toString());
        vo.setSimulated(true);
        return vo;
    }

    @Override
    public ChatResponseVO generateMarketingCopy(String description) {
        if (!hasRealApiKey()) {
            log.warn("未配置真实 AI API Key 或接入点 ID，使用模拟回复");
            return simulateMarketingCopy(description);
        }

        String systemPrompt = "你是一位资深电商营销策划师。请根据用户提供的信息，完成以下任务：\n" +
            "1. 为活动起一个吸引人的名称（15字以内）。\n" +
            "2. 撰写一段活动描述文案（50字以内），突出活动亮点。\n" +
            "3. 列出3条对该活动设计的改进建议。\n" +
            "4. 用中文回答，语气有吸引力、专业。\n" +
            "5. 分点清晰，便于阅读。";

        String userPrompt = "用户描述的活动信息：\n" + description;

        try {
            String reply = callApi(systemPrompt, userPrompt);
            ChatResponseVO vo = new ChatResponseVO();
            vo.setContent(reply.trim());
            vo.setSimulated(false);
            return vo;
        } catch (Exception e) {
            log.warn("AI 营销文案生成调用失败，降级为模拟回复", e);
            return simulateMarketingCopy(description);
        }
    }

    private ChatResponseVO simulateMarketingCopy(String description) {
        StringBuilder sb = new StringBuilder();
        sb.append("【AI 辅助活动方案】\n\n");
        sb.append("1. 活动名称：限时狂欢购\n");
        sb.append("2. 活动描述：精选好物限时特惠，全场满减叠加折扣，快来抢购！\n\n");
        sb.append("3. 改进建议：\n");
        sb.append("   - 建议明确活动起止时间，营造紧迫感。\n");
        sb.append("   - 可以增加限时秒杀环节，提升用户参与度。\n");
        sb.append("   - 搭配优惠券发放，进一步提高转化率。\n");

        ChatResponseVO vo = new ChatResponseVO();
        vo.setContent(sb.toString());
        vo.setSimulated(true);
        return vo;
    }

    /**
     * 调用 OpenAI 兼容 API（支持 DeepSeek、火山方舟等）
     */
    private String callApi(String systemPrompt, String userPrompt) throws Exception {
        String url = getApiUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = Map.of(
            "model", getModel(),
            "temperature", temperature,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
        }

        throw new RuntimeException("AI API 返回格式异常: " + response.getStatusCode());
    }
}