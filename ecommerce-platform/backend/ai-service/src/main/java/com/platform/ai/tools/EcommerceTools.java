package com.platform.ai.tools;

import com.platform.common.result.Result;
import com.platform.feign.api.BizServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 电商工具类 - 供 LLM Tool Calling 调用
 * 每个方法返回 LLM 可理解的字符串结果
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EcommerceTools {

    private final BizServiceClient bizServiceClient;

    /** 星期几中文映射 */
    private static final String[] WEEK_DAYS = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};

    /**
     * Tool 1: 查询商品信息（价格、库存、描述、评分）
     * Description for LLM: "查询指定商品的详细信息，包括价格、库存、描述和评分"
     */
    public String queryProductInfo(Long productId) {
        if (productId == null) {
            return "错误：缺少商品ID参数";
        }
        try {
            Result<Object> result = bizServiceClient.getProductById(productId);
            if (result != null && result.getCode() != null && result.getCode() == 200 && result.getData() != null) {
                Object data = result.getData();
                // data 可能是 LinkedHashMap 或 Product 对象
                String name = null, description = null;
                Object price = null, stock = null, rating = null, onSale = null;

                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) data;
                    name = map.get("name") != null ? map.get("name").toString() : null;
                    description = map.get("description") != null ? map.get("description").toString() : null;
                    price = map.get("price");
                    stock = map.get("stock");
                    rating = map.get("rating");
                    onSale = map.get("onSale");
                }

                if (name == null) {
                    return "未找到ID为 " + productId + " 的商品";
                }

                StringBuilder sb = new StringBuilder();
                sb.append("商品名称：").append(name).append("\n");
                sb.append("商品价格：").append(price != null ? "¥" + price : "暂无").append("\n");
                sb.append("库存数量：").append(stock != null ? stock + "件" : "暂无").append("\n");
                sb.append("商品评分：").append(rating != null ? rating + "分" : "暂无评分").append("\n");
                if (description != null && !description.isEmpty()) {
                    sb.append("商品描述：").append(description).append("\n");
                }
                sb.append("上架状态：").append(onSale != null && "1".equals(onSale.toString()) ? "已上架" : "已下架");
                return sb.toString();
            } else {
                return "未找到ID为 " + productId + " 的商品";
            }
        } catch (Exception e) {
            log.error("查询商品信息失败, productId={}", productId, e);
            return "查询商品信息失败：" + e.getMessage();
        }
    }

    /**
     * Tool 2: 查询用户信息（余额、VIP等级）
     * Description for LLM: "查询指定用户的账户信息，包括余额和VIP等级"
     */
    public String queryUserInfo(Long userId) {
        if (userId == null) {
            return "错误：缺少用户ID参数";
        }
        try {
            Result<Map<String, Object>> result = bizServiceClient.getUserById(userId);
            if (result != null && result.getCode() != null && result.getCode() == 200 && result.getData() != null) {
                Map<String, Object> data = result.getData();
                StringBuilder sb = new StringBuilder();
                sb.append("用户名：").append(data.get("username") != null ? data.get("username") : "未知").append("\n");
                sb.append("VIP等级：").append(data.get("vipLevel") != null ? "VIP" + data.get("vipLevel") : "普通用户").append("\n");
                sb.append("账户余额：").append(data.get("balance") != null ? "¥" + data.get("balance") : "¥0.00").append("\n");
                if (data.get("preference") != null && !data.get("preference").toString().isEmpty()) {
                    sb.append("用户偏好：").append(data.get("preference"));
                }
                return sb.toString();
            } else {
                return "未找到ID为 " + userId + " 的用户";
            }
        } catch (Exception e) {
            log.error("查询用户信息失败, userId={}", userId, e);
            return "查询用户信息失败：" + e.getMessage();
        }
    }

    /**
     * Tool 3: 查询用户订单
     * Description for LLM: "查询指定用户的订单列表，包括订单状态和商品信息"
     */
    public String queryUserOrders(Long userId) {
        if (userId == null) {
            return "错误：缺少用户ID参数";
        }
        // BizServiceClient 暂无订单查询方法，返回提示信息
        return "暂不支持订单查询功能，如需查询订单请前往\"我的订单\"页面查看。";
    }

    /**
     * Tool 4: 获取当前日期时间
     * Description for LLM: "获取当前的日期、时间和星期几"
     */
    public String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyy年M月d日"));
        String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String weekDay = WEEK_DAYS[now.getDayOfWeek().getValue() - 1];
        return dateStr + " " + weekDay + " " + timeStr;
    }

    /**
     * Tool 5: 查询商品评论概要
     * Description for LLM: "查询指定商品的最近评论和平均评分"
     */
    public String queryProductReviews(Long productId) {
        if (productId == null) {
            return "错误：缺少商品ID参数";
        }
        // BizServiceClient 暂无评论查询方法，返回提示信息
        return "暂无评论数据。您可以直接查看商品详情页下方的评论区获取最新用户评价。";
    }
}