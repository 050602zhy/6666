package com.platform.biz.controller;

import com.platform.biz.annotation.RequireRole;
import com.platform.biz.entity.Product;
import com.platform.biz.service.ProductService;
import com.platform.biz.vo.ProductDetailVO;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    @RequireRole("seller")
    public Result<Void> updateProduct(@RequestBody Product product) {
        productService.updateProduct(product);
        return Result.success();
    }

    @PostMapping("/image")
    @Operation(summary = "更新商品图片", description = "更新商品图片（base64或URL）")
    @RequireRole("seller")
    public Result<Void> updateImage(@RequestBody java.util.Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String imageUrl = body.get("imageUrl").toString();
        productService.updateImage(id, imageUrl);
        return Result.success();
    }

    @PutMapping("/onSale")
    @Operation(summary = "更新上下架状态", description = "设置商品的上架或下架状态")
    @RequireRole("seller")
    public Result<Void> updateOnSale(
            @Parameter(description = "商品ID", required = true, example = "1")
            @RequestParam(name = "id") Long id,
            @Parameter(description = "上架状态: 0-下架 1-上架", required = true, example = "1")
            @RequestParam(name = "onSale") Integer onSale) {
        productService.updateOnSale(id, onSale);
        return Result.success();
    }

    @GetMapping("/onSaleList")
    @Operation(summary = "获取上架商品列表", description = "获取所有上架商品列表（按ID升序）")
    public Result<List<Product>> onSaleList() {
        return Result.success(productService.onSaleList());
    }

    @PostMapping("/save")
    @Operation(summary = "发布商品", description = "卖家发布新商品")
    @RequireRole("seller")
    public Result<Void> save(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("currentUserId");
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(body.get("name").toString());
        product.setDescription(body.get("description") != null ? body.get("description").toString() : null);
        product.setPrice(new BigDecimal(body.get("price").toString()));
        product.setStock(Integer.valueOf(body.get("stock").toString()));
        productService.save(product);
        return Result.success();
    }

    @GetMapping("/myList")
    @Operation(summary = "查询我的商品列表", description = "获取当前登录卖家的商品列表")
    @RequireRole("seller")
    public Result<List<Product>> myList(HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("currentUserId");
        return Result.success(productService.listBySellerId(sellerId));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取商品详情", description = "获取商品详情（含评论列表）")
    public Result<ProductDetailVO> getProductDetail(
            @Parameter(description = "商品ID", required = true, example = "1")
            @PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @PostMapping("/buy")
    @Operation(summary = "购买商品", description = "购买商品：检查库存→临时锁定库存→创建订单→返回订单号")
    @RequireRole("buyer")
    public Result<String> buy(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long buyerId = Long.valueOf(body.get("buyerId").toString());
        Integer quantity = Integer.valueOf(body.get("quantity").toString());
        String orderNo = productService.buy(productId, buyerId, quantity);
        return Result.success(orderNo);
    }

    @PostMapping("/comment")
    @Operation(summary = "添加商品评论", description = "为商品添加评论和评分")
    @RequireRole("buyer")
    public Result<Void> addComment(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long orderId = Long.valueOf(body.get("orderId").toString());
        Long userId = Long.valueOf(body.get("userId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String content = body.get("content") != null ? body.get("content").toString() : null;
        productService.addComment(productId, orderId, userId, rating, content);
        return Result.success();
    }
}
