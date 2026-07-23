package com.platform.ai.config;

import com.platform.ai.tools.EcommerceTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Tool Calling 配置类
 * 构建 OpenAI 格式的 tool definitions，并提供按名称执行工具的方法
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolCallingConfig {

    private final EcommerceTools ecommerceTools;
    private final ObjectMapper objectMapper;

    /**
     * 构建 OpenAI 格式的 tools 数组，用于 API 请求
     */
    public List<Map<String, Object>> getToolDefinitions() {
        List<Map<String, Object>> tools = new ArrayList<>();

        // Tool 1: queryProductInfo
        tools.add(buildToolDefinition(
                "queryProductInfo",
                "查询指定商品的详细信息，包括价格、库存、描述和评分。当用户询问商品的价格、库存、质量、评分等问题时使用此工具。",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "productId", Map.of(
                                        "type", "integer",
                                        "description", "商品ID"
                                )
                        ),
                        "required", List.of("productId")
                )
        ));

        // Tool 2: queryUserInfo
        tools.add(buildToolDefinition(
                "queryUserInfo",
                "查询指定用户的账户信息，包括余额和VIP等级。当用户询问自己的余额、VIP等级、会员信息时使用此工具。",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "userId", Map.of(
                                        "type", "integer",
                                        "description", "用户ID"
                                )
                        ),
                        "required", List.of("userId")
                )
        ));

        // Tool 3: queryUserOrders
        tools.add(buildToolDefinition(
                "queryUserOrders",
                "查询指定用户的订单列表，包括订单状态和商品信息。当用户询问自己的订单、物流、历史购买记录时使用此工具。",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "userId", Map.of(
                                        "type", "integer",
                                        "description", "用户ID"
                                )
                        ),
                        "required", List.of("userId")
                )
        ));

        // Tool 4: getCurrentDateTime
        tools.add(buildToolDefinition(
                "getCurrentDateTime",
                "获取当前的日期、时间和星期几。当用户询问现在几点、今天几号、星期几、什么时候发货等与时间相关的问题时使用此工具。",
                Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "required", List.of()
                )
        ));

        // Tool 5: queryProductReviews
        tools.add(buildToolDefinition(
                "queryProductReviews",
                "查询指定商品的最近评论和平均评分。当用户询问其他人怎么评价这个商品、评论好不好、买家反馈等时使用此工具。",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "productId", Map.of(
                                        "type", "integer",
                                        "description", "商品ID"
                                )
                        ),
                        "required", List.of("productId")
                )
        ));

        return tools;
    }

    /**
     * 构建单个工具定义
     */
    private Map<String, Object> buildToolDefinition(String name, String description, Map<String, Object> parameters) {
        Map<String, Object> function = new HashMap<>();
        function.put("name", name);
        function.put("description", description);
        function.put("parameters", parameters);

        Map<String, Object> tool = new HashMap<>();
        tool.put("type", "function");
        tool.put("function", function);
        return tool;
    }

    /**
     * 根据工具名称和参数执行对应的工具方法
     *
     * @param toolName 工具名称
     * @param arguments 工具参数（从 LLM 传来的 JSON 对象）
     * @return 工具执行结果字符串
     */
    public String executeTool(String toolName, Map<String, Object> arguments) {
        try {
            switch (toolName) {
                case "queryProductInfo": {
                    Long productId = getLongArg(arguments, "productId");
                    log.info("Tool Calling: queryProductInfo({})", productId);
                    return ecommerceTools.queryProductInfo(productId);
                }
                case "queryUserInfo": {
                    Long userId = getLongArg(arguments, "userId");
                    log.info("Tool Calling: queryUserInfo({})", userId);
                    return ecommerceTools.queryUserInfo(userId);
                }
                case "queryUserOrders": {
                    Long userId = getLongArg(arguments, "userId");
                    log.info("Tool Calling: queryUserOrders({})", userId);
                    return ecommerceTools.queryUserOrders(userId);
                }
                case "getCurrentDateTime": {
                    log.info("Tool Calling: getCurrentDateTime()");
                    return ecommerceTools.getCurrentDateTime();
                }
                case "queryProductReviews": {
                    Long productId = getLongArg(arguments, "productId");
                    log.info("Tool Calling: queryProductReviews({})", productId);
                    return ecommerceTools.queryProductReviews(productId);
                }
                default:
                    log.warn("未知的工具名称: {}", toolName);
                    return "未知的工具：" + toolName;
            }
        } catch (Exception e) {
            log.error("执行工具 {} 失败", toolName, e);
            return "工具执行失败：" + e.getMessage();
        }
    }

    /**
     * 从参数 Map 中安全地获取 Long 值
     */
    private Long getLongArg(Map<String, Object> arguments, String key) {
        Object val = arguments.get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        try {
            return Long.valueOf(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}