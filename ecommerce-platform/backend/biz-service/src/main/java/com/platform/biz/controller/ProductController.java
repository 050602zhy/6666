package com.platform.biz.controller;

import com.platform.biz.entity.Product;
import com.platform.biz.service.ProductService;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制器
 */
@RestController
@RequestMapping("/biz/product")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品信息的增删改查接口")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    @Operation(summary = "查询商品列表", description = "获取所有商品信息列表")
    public Result<List<Product>> list() {
        return Result.success(productService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询商品", description = "根据商品ID获取商品详细信息")
    public Result<Product> getById(
            @Parameter(description = "商品ID", required = true, example = "1")
            @PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PutMapping
    @Operation(summary = "更新商品信息", description = "更新商品描述、价格等信息（只更新非null字段）")
    public Result<Void> updateProduct(@RequestBody Product product) {
        productService.updateProduct(product);
        return Result.success();
    }

    @PutMapping("/image")
    @Operation(summary = "更新商品图片", description = "更新商品主图URL")
    public Result<Void> updateImage(
            @Parameter(description = "商品ID", required = true, example = "1")
            @RequestParam Long id,
            @Parameter(description = "图片URL", required = true, example = "https://example.com/image.jpg")
            @RequestParam String imageUrl) {
        productService.updateImage(id, imageUrl);
        return Result.success();
    }

    @PutMapping("/onSale")
    @Operation(summary = "更新上下架状态", description = "设置商品的上架或下架状态")
    public Result<Void> updateOnSale(
            @Parameter(description = "商品ID", required = true, example = "1")
            @RequestParam Long id,
            @Parameter(description = "上架状态: 0-下架 1-上架", required = true, example = "1")
            @RequestParam Integer onSale) {
        productService.updateOnSale(id, onSale);
        return Result.success();
    }
}