package com.platform.biz.controller;

import com.platform.common.result.Result;
import com.platform.biz.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理控制器
 */
@RestController
@RequestMapping("/biz/product")
@RequiredArgsConstructor
// Knife4j/SpringDoc 接口分组标签，name 为显示名称，description 为详细描述
@Tag(name = "商品管理", description = "商品信息的增删改查接口")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    // Operation 描述接口功能，summary 为简短摘要
    @Operation(summary = "根据ID查询商品", description = "根据商品ID获取商品详细信息")
    public Result<Object> getById(
            // Parameter 描述参数含义，example 提供示例值
            @Parameter(description = "商品ID", required = true, example = "1")
            @PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @GetMapping("/list")
    @Operation(summary = "查询商品列表", description = "获取所有商品信息列表")
    public Result<Object> list() {
        return Result.success(productService.list());
    }
}
