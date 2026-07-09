package com.platform.biz.controller;

import com.platform.common.result.Result;
import com.platform.biz.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/biz/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<Long> create(@RequestBody Object orderDTO) {
        return Result.success(orderService.create(orderDTO));
    }

    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }
}
