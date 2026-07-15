package com.platform.biz.controller;

import com.platform.biz.annotation.RequireRole;
import com.platform.biz.entity.Order;
import com.platform.biz.service.OrderService;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/biz/order")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单创建、查询、发货、退货、评分接口")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "创建新订单")
    public Result<Long> create(@RequestBody Object orderDTO) {
        return Result.success(orderService.create(orderDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询订单", description = "根据订单ID查询订单详情")
    public Result<Object> getById(
            @Parameter(description = "订单ID", required = true, example = "1")
            @PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "订单列表", description = "查询用户订单列表（90天内）")
    public Result<List<Order>> list(
            @Parameter(description = "用户ID", required = true, example = "1")
            @RequestParam(name = "userId") String userId,
            @Parameter(description = "角色: buyer-买家 seller-卖家", required = true, example = "buyer")
            @RequestParam(name = "role") String role) {
        try {
            Long uid = Long.valueOf(userId);
            return Result.success(orderService.list(uid, role));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "查询订单失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @PutMapping("/ship")
    @Operation(summary = "卖家同意出货", description = "卖家同意出货：减少库存、资金转移、更新订单状态为已发货")
    @RequireRole("seller")
    public Result<Void> ship(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        orderService.ship(orderId);
        return Result.success();
    }

    @PutMapping("/complete")
    @Operation(summary = "买家确认收货", description = "买家确认收货，更新订单状态为已完成")
    @RequireRole("buyer")
    public Result<Void> complete(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        orderService.complete(orderId);
        return Result.success();
    }

    @PostMapping("/refund")
    @Operation(summary = "申请退货", description = "买家申请退货")
    @RequireRole("buyer")
    public Result<Void> refund(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String reason = body.get("reason") != null ? body.get("reason").toString() : null;
        orderService.refund(orderId, reason);
        return Result.success();
    }

    @PutMapping("/refund/handle")
    @Operation(summary = "处理退货", description = "卖家处理退货申请: 1-同意 2-拒绝")
    @RequireRole("seller")
    public Result<Void> handleRefund(@RequestBody Map<String, Object> body) {
        Long refundId = Long.valueOf(body.get("refundId").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        String reply = body.get("reply") != null ? body.get("reply").toString() : null;
        orderService.handleRefund(refundId, status, reply);
        return Result.success();
    }

    @PutMapping("/rate")
    @Operation(summary = "订单评分", description = "买家对订单进行评分和评价")
    @RequireRole("buyer")
    public Result<Void> rate(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String content = body.get("content") != null ? body.get("content").toString() : null;
        orderService.rate(orderId, rating, content);
        return Result.success();
    }
}
