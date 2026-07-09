package com.platform.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
@Schema(description = "商品实体")
public class Product extends BaseEntity {

    @Schema(description = "卖家ID")
    @TableField("seller_id")
    private Long sellerId;

    @Schema(description = "商品名称")
    @TableField("name")
    private String name;

    @Schema(description = "商品描述")
    @TableField("description")
    private String description;

    @Schema(description = "商品图片")
    @TableField("image")
    private String image;

    @Schema(description = "商品价格")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "库存数量")
    @TableField("stock")
    private Integer stock;

    @Schema(description = "是否上架：0-下架，1-上架")
    @TableField("on_sale")
    private Integer onSale;

    @Schema(description = "商品评分")
    @TableField("rating")
    private BigDecimal rating;
}
