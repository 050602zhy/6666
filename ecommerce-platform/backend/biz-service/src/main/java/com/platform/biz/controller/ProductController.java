package com.platform.biz.controller;

import com.platform.common.result.Result;
import com.platform.biz.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理控制器
 */
@RestController
@RequestMapping("/biz/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @GetMapping("/list")
    public Result<Object> list() {
        return Result.success(productService.list());
    }
}
