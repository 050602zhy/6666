package com.platform.biz.vo;

import com.platform.biz.entity.Product;
import com.platform.biz.entity.ProductReview;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 商品详情VO（含评论列表）
 */
@Data
@Schema(description = "商品详情")
public class ProductDetailVO {

    @Schema(description = "商品信息")
    private Product product;

    @Schema(description = "评论列表")
    private List<ProductReview> reviews;
}
