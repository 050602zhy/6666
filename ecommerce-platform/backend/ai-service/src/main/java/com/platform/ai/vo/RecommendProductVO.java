package com.platform.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 推荐商品VO
 */
@Data
@Schema(description = "推荐商品信息")
public class RecommendProductVO {

    @Schema(description = "商品ID", example = "1")
    private Long id;

    @Schema(description = "商品名称", example = "高端蓝牙降噪耳机")
    private String name;

    @Schema(description = "商品图片", example = "https://example.com/image.jpg")
    private String image;

    @Schema(description = "商品价格", example = "999.00")
    private BigDecimal price;

    @Schema(description = "折后价格（活动商品）", example = "799.00")
    private BigDecimal discountPrice;

    @Schema(description = "商品评分（截断小数后显示）", example = "4")
    private Integer rating;

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "推荐理由", example = "符合您的购物偏好")
    private String reason;

    @Schema(description = "匹配度百分比", example = "90")
    private Integer matchScore;
}
