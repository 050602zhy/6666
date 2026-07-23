package com.platform.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.ai.config.ToolCallingConfig;
import com.platform.ai.service.AiChatService;
import com.platform.ai.tools.EcommerceTools;
import com.platform.ai.vo.ChatResponseVO;
import com.platform.ai.vo.MarketingCopyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI 聊天服务实现
 * 直接通过 HTTP 调用 OpenAI 兼容 API（支持 DeepSeek、火山方舟等）
 * chatAboutProduct 支持 Tool Calling，让 LLM 调用工具查询实时数据
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
    private final ToolCallingConfig toolCallingConfig;
    private final EcommerceTools ecommerceTools;

    public AiChatServiceImpl(ToolCallingConfig toolCallingConfig, EcommerceTools ecommerceTools) {
        this.toolCallingConfig = toolCallingConfig;
        this.ecommerceTools = ecommerceTools;
    }

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

    /** Tool Calling 最大迭代次数 */
    private static final int MAX_TOOL_ITERATIONS = 5;

    @Override
    public ChatResponseVO chatAboutProduct(String productName, String productDesc, String question,
                                            Long productId, Long userId, Double userBalance, Integer userVipLevel, String productContext) {
        if (!hasRealApiKey()) {
            log.warn("未配置真实 AI API Key 或接入点 ID，使用模拟回复");
            return simulateChatReply(productName, question);
        }

        // === 直接使用前端传入的实时数据构建上下文，不依赖 Feign 跨服务调用 ===
        StringBuilder realtimeData = new StringBuilder();

        // 1. 商品实时信息（前端传入的 productContext）
        if (productContext != null && !productContext.isEmpty()) {
            try {
                StringBuilder productInfo = new StringBuilder();
                productInfo.append("商品名称：").append(productName).append("\n");
                // 解析前端传来的 JSON
                JsonNode ctx = objectMapper.readTree(productContext);
                if (ctx.has("price")) productInfo.append("商品价格：¥").append(ctx.get("price").asText()).append("\n");
                if (ctx.has("discountPrice") && ctx.get("discountPrice").asDouble() > 0) {
                    productInfo.append("折后价格：¥").append(ctx.get("discountPrice").asText()).append("\n");
                    if (ctx.has("discount")) productInfo.append("折扣：").append(ctx.get("discount").asText()).append("折\n");
                }
                if (ctx.has("stock")) productInfo.append("库存数量：").append(ctx.get("stock").asInt()).append("件\n");
                if (ctx.has("rating")) productInfo.append("商品评分：").append(ctx.get("rating").asText()).append("分\n");
                if (ctx.has("onSale")) productInfo.append("上架状态：").append("1".equals(ctx.get("onSale").asText()) ? "已上架" : "已下架").append("\n");
                realtimeData.append("【商品实时信息】\n").append(productInfo).append("\n");
            } catch (Exception e) {
                log.warn("解析 productContext 失败", e);
            }
        }

        // 2. 用户信息（前端直接传入，无需 Feign）
        if (userId != null) {
            StringBuilder userInfo = new StringBuilder();
            userInfo.append("用户ID：").append(userId).append("\n");
            if (userVipLevel != null) {
                userInfo.append("VIP等级：VIP").append(userVipLevel).append("\n");
            } else {
                userInfo.append("VIP等级：普通用户\n");
            }
            if (userBalance != null) {
                userInfo.append("账户余额：¥").append(userBalance).append("\n");
            }
            realtimeData.append("【用户信息】\n").append(userInfo).append("\n");
        }

        // 3. 当前时间
        try {
            String dateTime = ecommerceTools.getCurrentDateTime();
            realtimeData.append("【当前时间】\n").append(dateTime).append("\n\n");
        } catch (Exception e) {
            log.warn("获取当前时间失败", e);
        }

        // 构建增强版 system prompt
        String systemPrompt = buildRealtimeSystemPrompt(productName, productDesc, realtimeData.toString());

        // 使用普通对话模式（数据已在 prompt 中）
        try {
            String reply = callApi(systemPrompt, "用户问题：" + question);
            ChatResponseVO vo = new ChatResponseVO();
            vo.setContent(reply.trim());
            vo.setSimulated(false);
            return vo;
        } catch (Exception e) {
            log.warn("AI 客服调用失败: {}", e.getMessage());
        }

        // 最终兜底：模拟回复
        return simulateChatReply(productName, question);
    }

    /**
     * 构建带有实时数据的 system prompt
     * 在调用 AI 之前已经把商品库存、价格、评分等真实数据嵌入到 prompt 中
     */
    private String buildRealtimeSystemPrompt(String productName, String productDesc, String realtimeData) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一家电商平台的智能客服，专门回答关于商品「").append(productName).append("」的问题。\n");
        sb.append("商品描述：").append(productDesc != null ? productDesc : "暂无描述").append("\n\n");

        // 嵌入实时数据
        if (realtimeData != null && !realtimeData.isEmpty()) {
            sb.append("=== 以下是最新的实时数据，请基于这些数据回答用户问题 ===\n\n");
            sb.append(realtimeData);
            sb.append("=== 实时数据结束 ===\n\n");
        }

        sb.append("重要规则：\n");
        sb.append("1. 当用户询问价格、库存、评分等数据时，请直接引用上面提供的实时数据回答，不要编造或猜测。\n");
        sb.append("2. 如果用户问余额、VIP等级等问题，使用上面提供的用户实时信息回答。\n");
        sb.append("3. 如果用户问时间问题，使用上面提供的当前时间回答。\n");
        sb.append("4. 只回答与该商品相关的问题（如功能、用途、质量、售后、价格、库存等）。\n");
        sb.append("5. 如果用户问的问题与商品无关，请严格回复：\"亲，你的问题超出我的范畴了，请换个问题吧\"\n");
        sb.append("6. 用友好、专业的语气回答，称呼用户为\"亲\"，结尾可以加个表情。\n");
        sb.append("7. 回答尽量简洁，不超过150字。");

        return sb.toString();
    }

    /**
     * 构建 Tool Calling 模式的 system prompt（作为备用方案）
     */
    private String buildToolCallingSystemPrompt(String productName, String productDesc, Long productId, Long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一家电商平台的智能客服，专门回答关于商品「").append(productName).append("」的问题。\n");
        sb.append("商品描述：").append(productDesc != null ? productDesc : "暂无描述").append("\n\n");

        if (productId != null) {
            sb.append("当前商品ID：").append(productId).append("\n");
        }
        if (userId != null) {
            sb.append("当前用户ID：").append(userId).append("\n");
        }
        sb.append("\n");

        sb.append("你可以使用以下工具来查询实时数据：\n");
        sb.append("- queryProductInfo：查询商品的实时价格、库存、描述和评分\n");
        sb.append("- queryUserInfo：查询用户的余额和VIP等级\n");
        sb.append("- queryUserOrders：查询用户的订单列表\n");
        sb.append("- getCurrentDateTime：获取当前日期和时间\n");
        sb.append("- queryProductReviews：查询商品评论概要\n\n");

        sb.append("重要规则：\n");
        sb.append("1. 当用户询问价格、库存、评分等实时数据时，请务必调用 queryProductInfo 工具获取最新数据，不要猜测或编造数据。\n");
        sb.append("2. 当用户询问余额、VIP等级等个人账户信息时，请调用 queryUserInfo 工具。\n");
        sb.append("3. 当用户询问订单、物流等信息时，请调用 queryUserOrders 工具。\n");
        sb.append("4. 当用户询问时间相关问题（如什么时候发货、几天到）时，请调用 getCurrentDateTime 工具获取当前时间。\n");
        sb.append("5. 当用户询问评论、评价等信息时，请调用 queryProductReviews 工具。\n");
        sb.append("6. 调用工具时，如果需要 productId 或 userId 但用户没有提供，请使用上面给出的当前商品ID和用户ID。\n");
        sb.append("7. 获取到实时数据后，基于真实数据回答用户问题。\n");
        sb.append("8. 只回答与该商品相关的问题（如功能、用途、质量、售后、价格、库存等）。\n");
        sb.append("9. 如果用户问的问题与商品无关，请严格回复：\"亲，你的问题超出我的范畴了，请换个问题吧\"\n");
        sb.append("10. 用友好、专业的语气回答，称呼用户为\"亲\"，结尾可以加个表情。\n");
        sb.append("11. 回答尽量简洁，不超过150字。");

        return sb.toString();
    }

    /**
     * 带 Tool Calling 的 API 调用
     * 实现完整的 tool calling 循环：发送消息 -> 检查 finish_reason -> 执行工具 -> 回传结果 -> 继续循环
     */
    private String callApiWithTools(String systemPrompt, String userPrompt, Long productId, Long userId) throws Exception {
        String url = getApiUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // Step 1: 构建 messages 数组
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userPrompt));

        // Step 2: 构建请求体（含 tools）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", getModel());
        requestBody.put("temperature", temperature);
        requestBody.put("messages", messages);
        requestBody.put("tools", toolCallingConfig.getToolDefinitions());
        requestBody.put("tool_choice", "auto");

        // Step 3: Tool Calling 循环
        for (int i = 0; i < MAX_TOOL_ITERATIONS; i++) {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("AI API 返回异常: " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new RuntimeException("AI API 返回 choices 为空");
            }

            JsonNode choice = choices.get(0);
            JsonNode message = choice.path("message");
            // finish_reason 可能在 choice 或 message 层级
            String finishReason = choice.path("finish_reason").asText();
            if (finishReason == null || finishReason.isEmpty()) {
                finishReason = message.path("finish_reason").asText();
            }

            // 检查是否有 tool_calls（即使 finish_reason 不是 "tool_calls"）
            JsonNode toolCalls = message.path("tool_calls");
            boolean hasToolCalls = toolCalls.isArray() && !toolCalls.isEmpty();

            if ("tool_calls".equals(finishReason) || hasToolCalls) {
                // LLM 请求调用工具
                log.info("Tool Calling 迭代 {}/{}: LLM 请求调用工具 (finish_reason={})", i + 1, MAX_TOOL_ITERATIONS, finishReason);

                // 将 assistant 消息（含 tool_calls）加入对话历史
                @SuppressWarnings("unchecked")
                Map<String, Object> assistantMsg = objectMapper.convertValue(message, Map.class);
                messages.add(assistantMsg);

                // 执行每个 tool call
                for (JsonNode toolCall : toolCalls) {
                    String toolName = toolCall.path("function").path("name").asText();
                    String argumentsStr = toolCall.path("function").path("arguments").asText();
                    String toolCallId = toolCall.path("id").asText();

                    log.info("执行工具: {} (callId={})", toolName, toolCallId);

                    // 解析参数
                    Map<String, Object> args;
                    try {
                        args = objectMapper.readValue(argumentsStr, Map.class);
                    } catch (Exception e) {
                        args = new HashMap<>();
                    }

                    // 自动注入 productId 和 userId（如果 LLM 未提供但有上下文值）
                    if (args.containsKey("productId") && args.get("productId") == null && productId != null) {
                        args.put("productId", productId);
                    }
                    if (args.containsKey("userId") && args.get("userId") == null && userId != null) {
                        args.put("userId", userId);
                    }

                    // 执行工具
                    String toolResult = toolCallingConfig.executeTool(toolName, args);
                    log.info("工具 {} 返回结果: {}", toolName, toolResult.length() > 100 ? toolResult.substring(0, 100) + "..." : toolResult);

                    // 将工具结果加入消息
                    Map<String, Object> toolResultMsg = new HashMap<>();
                    toolResultMsg.put("role", "tool");
                    toolResultMsg.put("tool_call_id", toolCallId);
                    toolResultMsg.put("content", toolResult);
                    messages.add(toolResultMsg);
                }

                // 更新请求体，继续循环
                requestBody.put("messages", messages);

            } else {
                // LLM 给出最终回答（finish_reason 为 "stop" 或其他）
                String content = message.path("content").asText();
                if (content == null || content.isEmpty()) {
                    // content 为空，可能是 API 不支持 tools 或模型拒绝回答
                    log.warn("AI 返回了空内容, finish_reason={}, response={}", finishReason, response.getBody());
                    throw new RuntimeException("AI 返回空内容，可能当前模型不支持 Tool Calling");
                }
                return content;
            }
        }

        throw new RuntimeException("Tool Calling 超过最大迭代次数(" + MAX_TOOL_ITERATIONS + "次)");
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
    public MarketingCopyVO generateMarketingCopy(String description) {
        if (!hasRealApiKey()) {
            log.warn("未配置真实 AI API Key 或接入点 ID，使用模拟回复");
            return simulateMarketingCopy(description);
        }

        String systemPrompt = "你是一位资深电商营销策划师。请根据用户提供的活动信息，生成一份结构化的活动方案。\n\n" +
            "要求：\n" +
            "1. 活动名称：吸引人，15字以内。\n" +
            "2. 活动描述：突出活动亮点，50字以内。\n" +
            "3. 改进建议：3条具体的改进建议。\n\n" +
            "请严格按照以下 JSON 格式输出（不要输出任何额外文字，不要包含 markdown 代码块标记）：\n" +
            "{\n" +
            "  \"name\": \"活动名称\",\n" +
            "  \"description\": \"活动描述文案\",\n" +
            "  \"suggestions\": [\"建议1\", \"建议2\", \"建议3\"]\n" +
            "}";

        String userPrompt = "用户描述的活动信息：\n" + description;

        try {
            String reply = callApi(systemPrompt, userPrompt);
            return parseMarketingCopy(reply.trim(), false);
        } catch (Exception e) {
            log.warn("AI 营销文案生成调用失败，降级为模拟回复", e);
            return simulateMarketingCopy(description);
        }
    }

    /**
     * 解析 AI 返回的营销文案，尝试从文本中提取结构化字段
     */
    private MarketingCopyVO parseMarketingCopy(String rawText, boolean simulated) {
        MarketingCopyVO vo = new MarketingCopyVO();
        vo.setRawContent(rawText);
        vo.setSimulated(simulated);

        if (rawText == null || rawText.isEmpty()) {
            return vo;
        }

        // 尝试1: 直接解析 JSON（AI 可能输出纯 JSON 或带代码块包裹
        String jsonStr = extractJson(rawText);
        if (jsonStr != null) {
            try {
                JsonNode root = objectMapper.readTree(jsonStr);
                String name = root.path("name").asText(null);
                String desc = root.path("description").asText(null);
                List<String> suggestions = new ArrayList<>();
                JsonNode suggNode = root.path("suggestions");
                if (suggNode.isArray()) {
                    for (JsonNode s : suggNode) {
                        suggestions.add(s.asText());
                    }
                }
                if (name != null && !name.isEmpty()) {
                    vo.setName(name);
                }
                if (desc != null && !desc.isEmpty()) {
                    vo.setDescription(desc);
                }
                if (!suggestions.isEmpty()) {
                    vo.setSuggestions(suggestions);
                }
                return vo;
            } catch (Exception e) {
                log.warn("解析 AI 营销文案 JSON 失败，尝试正则提取", e);
            }
        }

        // 尝试2: 用正则从文本中提取
        String name = extractByRegex(rawText, "(?:活动名称|名称)[：:]+\\s*(.+?)(?:\\n|$)");
        String desc = extractByRegex(rawText, "(?:活动描述|描述)[：:]+\\s*(.+?)(?:\\n\\n|\\n\\d|$)");
        List<String> suggestions = extractSuggestions(rawText);

        if (name != null && !name.isEmpty()) {
            vo.setName(name.trim());
        }
        if (desc != null && !desc.isEmpty()) {
            vo.setDescription(desc.trim());
        }
        if (!suggestions.isEmpty()) {
            vo.setSuggestions(suggestions);
        }

        return vo;
    }

    /**
     * 从文本中提取 JSON 字符串（支持 ```json ... ``` 包裹和裸 JSON）
     */
    private String extractJson(String text) {
        if (text == null) return null;
        // 优先匹配代码块中的 JSON
        Pattern codeBlockPattern = Pattern.compile("```(?:json)?\\s*\\n?(\\{[\\s\\S]*?\\})\\s*```", Pattern.CASE_INSENSITIVE);
        Matcher m = codeBlockPattern.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        // 匹配裸 JSON 对象（以 { 开头，以 } 结尾）
        String trimmed = text.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }
        // 尝试在文本中找到第一个 { 和最后一个 } 之间的内容
        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1);
        }
        return null;
    }

    /**
     * 使用正则提取单个字段
     */
    private String extractByRegex(String text, String regex) {
        try {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(text);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * 从文本中提取建议列表
     */
    private List<String> extractSuggestions(String text) {
        List<String> result = new ArrayList<>();
        if (text == null) return result;

        // 匹配 " - xxx" 或 "1. xxx" 或 "1、xxx 格式的建议条目
        Pattern p = Pattern.compile("(?:^|\\n)\\s*(?:[-*]|\\d+[.、])\\s*(.+?)(?=\\n\\s*(?:[-*]|\\d+[.、])|$)", Pattern.MULTILINE);
        Matcher m = p.matcher(text);
        while (m.find() && result.size() < 5) {
            String s = m.group(1).trim();
            if (!s.isEmpty() && s.length() > 3) {
                result.add(s);
            }
        }
        return result;
    }

    private MarketingCopyVO simulateMarketingCopy(String description) {
        MarketingCopyVO vo = new MarketingCopyVO();
        vo.setName("限时狂欢购");
        vo.setDescription("精选好物限时特惠，全场满减叠加折扣，快来抢购！");
        vo.setSuggestions(List.of(
            "建议明确活动起止时间，营造紧迫感。",
            "可以增加限时秒杀环节，提升用户参与度。",
            "搭配优惠券发放，进一步提高转化率。"
        ));
        vo.setRawContent("【AI 辅助活动方案】\n\n" +
            "1. 活动名称：限时狂欢购\n" +
            "2. 活动描述：精选好物限时特惠，全场满减叠加折扣，快来抢购！\n\n" +
            "3. 改进建议：\n" +
            "   - 建议明确活动起止时间，营造紧迫感。\n" +
            "   - 可以增加限时秒杀环节，提升用户参与度。\n" +
            "   - 搭配优惠券发放，进一步提高转化率。");
        vo.setSimulated(true);
        return vo;
    }

    @Override
    public ChatResponseVO chatAboutPlaza(String question, String productsJson,
                                          Long userId, Double userBalance, Integer userVipLevel) {
        if (!hasRealApiKey()) {
            log.warn("未配置真实 AI API Key 或接入点 ID，使用模拟回复");
            return simulatePlazaChatReply(question, productsJson);
        }

        // === 构建实时数据上下文 ===
        StringBuilder realtimeData = new StringBuilder();

        // 1. 商品列表信息（前端传入的所有上架商品）
        if (productsJson != null && !productsJson.isEmpty()) {
            try {
                StringBuilder productsInfo = new StringBuilder();
                JsonNode products = objectMapper.readTree(productsJson);
                if (products.isArray()) {
                    productsInfo.append("当前平台共有 ").append(products.size()).append(" 件上架商品：\n\n");
                    int index = 1;
                    for (JsonNode p : products) {
                        productsInfo.append(index).append(". ");
                        if (p.has("name")) productsInfo.append(p.get("name").asText());
                        if (p.has("id")) productsInfo.append("（ID: ").append(p.get("id").asText()).append("）");
                        productsInfo.append("\n");
                        if (p.has("description") && !p.get("description").asText().isEmpty()) {
                            productsInfo.append("   描述：").append(truncateText(p.get("description").asText(), 60)).append("\n");
                        }
                        if (p.has("price")) {
                            double price = p.get("price").asDouble();
                            productsInfo.append("   价格：¥").append(String.format("%.2f", price));
                            // 折扣信息
                            if (p.has("discountPrice") && p.get("discountPrice").asDouble() > 0
                                    && p.get("discountPrice").asDouble() < price) {
                                productsInfo.append("，折后价：¥").append(String.format("%.2f", p.get("discountPrice").asDouble()));
                                if (p.has("discount")) {
                                    productsInfo.append("（").append(String.format("%.1f", p.get("discount").asDouble() * 10)).append("折）");
                                }
                            }
                            productsInfo.append("\n");
                        }
                        if (p.has("stock")) productsInfo.append("   库存：").append(p.get("stock").asInt()).append("件\n");
                        if (p.has("rating")) productsInfo.append("   评分：").append(p.get("rating").asText()).append("分\n");
                        if (p.has("sellerName")) productsInfo.append("   卖家：").append(p.get("sellerName").asText()).append("\n");
                        productsInfo.append("\n");
                        index++;
                    }
                }
                realtimeData.append("【商品广场商品列表】\n").append(productsInfo).append("\n");
            } catch (Exception e) {
                log.warn("解析 productsJson 失败", e);
            }
        }

        // 2. 用户信息
        if (userId != null) {
            StringBuilder userInfo = new StringBuilder();
            userInfo.append("用户ID：").append(userId).append("\n");
            if (userVipLevel != null) {
                userInfo.append("VIP等级：VIP").append(userVipLevel).append("\n");
            } else {
                userInfo.append("VIP等级：普通用户\n");
            }
            if (userBalance != null) {
                userInfo.append("账户余额：¥").append(userBalance).append("\n");
            }
            realtimeData.append("【用户信息】\n").append(userInfo).append("\n");
        }

        // 3. 当前时间
        try {
            String dateTime = ecommerceTools.getCurrentDateTime();
            realtimeData.append("【当前时间】\n").append(dateTime).append("\n\n");
        } catch (Exception e) {
            log.warn("获取当前时间失败", e);
        }

        // 构建广场AI的 system prompt
        String systemPrompt = buildPlazaSystemPrompt(realtimeData.toString());

        // 调用AI
        try {
            String reply = callApi(systemPrompt, "用户问题：" + question);
            ChatResponseVO vo = new ChatResponseVO();
            vo.setContent(reply.trim());
            vo.setSimulated(false);
            return vo;
        } catch (Exception e) {
            log.warn("AI 广场咨询调用失败: {}", e.getMessage());
        }

        // 兜底：模拟回复
        return simulatePlazaChatReply(question, productsJson);
    }

    /**
     * 构建商品广场AI的 system prompt
     */
    private String buildPlazaSystemPrompt(String realtimeData) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一家电商平台的智能导购助手，名叫「广场AI小助手」。\n");
        sb.append("你的职责是帮助用户在商品广场中找到合适的商品，回答关于商品的各种问题。\n\n");

        // 嵌入实时数据
        if (realtimeData != null && !realtimeData.isEmpty()) {
            sb.append("=== 以下是当前商品广场的实时数据，请基于这些数据回答用户问题 ===\n\n");
            sb.append(realtimeData);
            sb.append("=== 实时数据结束 ===\n\n");
        }

        sb.append("你可以做的事情：\n");
        sb.append("1. 商品推荐：根据用户需求（如预算、用途、偏好等）推荐合适的商品\n");
        sb.append("2. 商品比较：对比多个商品的价格、特点、评分等\n");
        sb.append("3. 商品查询：回答关于某个或某些商品的具体问题（价格、库存、折扣等）\n");
        sb.append("4. 购物建议：给出购买建议和注意事项\n");
        sb.append("5. 活动介绍：介绍当前有折扣活动的商品\n\n");

        sb.append("重要规则：\n");
        sb.append("1. 所有回答必须基于上面提供的实时商品数据，不要编造不存在的商品或数据。\n");
        sb.append("2. 如果用户询问某个具体商品但数据中没有，请告知用户该商品未找到或已下架。\n");
        sb.append("3. 推荐商品时，要说明推荐理由，并列出商品名称、价格、折扣等关键信息。\n");
        sb.append("4. 如果问余额、VIP等级等问题，使用上面提供的用户实时信息回答。\n");
        sb.append("5. 如果问时间问题，使用上面提供的当前时间回答。\n");
        sb.append("6. 用友好、热情、专业的语气回答，称呼用户为\"亲\"。\n");
        sb.append("7. 回答尽量简洁明了，重要信息可以分点列出。\n");
        sb.append("8. 如果用户的问题与商品购物无关，请回复：\"亲，我是商品广场的AI导购助手，主要帮您解答商品相关问题哦~\"");

        return sb.toString();
    }

    /**
     * 截断文本
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    /**
     * 商品广场AI模拟回复
     */
    private ChatResponseVO simulatePlazaChatReply(String question, String productsJson) {
        String q = question.toLowerCase();
        String reply;

        // 解析商品列表，提取商品名称
        List<String> productNames = new ArrayList<>();
        if (productsJson != null && !productsJson.isEmpty()) {
            try {
                JsonNode products = objectMapper.readTree(productsJson);
                if (products.isArray()) {
                    for (JsonNode p : products) {
                        if (p.has("name")) {
                            productNames.add(p.get("name").asText());
                        }
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        if (q.contains("推荐") || q.contains("买什么") || q.contains("选哪个") || q.contains("什么好")) {
            if (productNames.isEmpty()) {
                reply = "亲，目前商品广场还没有上架商品哦，敬请期待~";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("亲，为您推荐以下热门商品：\n\n");
                for (int i = 0; i < Math.min(3, productNames.size()); i++) {
                    sb.append(i + 1).append(". ").append(productNames.get(i)).append("\n");
                }
                sb.append("\n这些都是平台上的优质商品，您可以点击查看详情哦~");
                reply = sb.toString();
            }
        } else if (q.contains("便宜") || q.contains("优惠") || q.contains("折扣") || q.contains("活动")) {
            reply = "亲，平台经常有优惠活动哦！您可以在商品广场中查看带有折扣标签的商品，享受超值优惠~ 点击商品卡片即可查看详情和购买！";
        } else if (q.contains("价格") || q.contains("多少钱")) {
            if (productNames.isEmpty()) {
                reply = "亲，目前商品广场还没有上架商品哦~";
            } else {
                reply = "亲，商品价格因商品而异。您可以在商品广场中浏览所有商品，点击任意商品卡片查看详细价格信息哦~";
            }
        } else if (q.contains("你好") || q.contains("在吗") || q.contains("hi") || q.contains("hello")) {
            reply = "亲，您好！我是商品广场的AI导购助手，有什么可以帮您的吗？您可以问我关于商品推荐、价格比较、活动介绍等问题哦~";
        } else if (q.contains("你是谁") || q.contains("介绍")) {
            reply = "亲，我是商品广场的AI导购助手！我可以帮您：\n1. 推荐合适的商品\n2. 比较不同商品\n3. 查询商品信息\n4. 介绍优惠活动\n有什么想了解的尽管问我吧~";
        } else {
            if (!productNames.isEmpty()) {
                reply = "亲，我是商品广场的AI导购助手，主要帮您解答商品相关问题哦~ 您可以问我关于「" + productNames.get(0) + "」等商品的问题，或者让我为您推荐商品~";
            } else {
                reply = "亲，我是商品广场的AI导购助手，主要帮您解答商品相关问题哦~";
            }
        }

        ChatResponseVO vo = new ChatResponseVO();
        vo.setContent(reply);
        vo.setSimulated(true);
        return vo;
    }

    /**
     * 调用 OpenAI 兼容 API（不带 Tool Calling，用于 analyzeComments 和 generateMarketingCopy）
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
