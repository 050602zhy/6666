package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品实体
 * 对应表: product
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 卖家ID */
    private Long sellerId;

    /** 分类ID */
    private Long categoryId;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String description;

    /** 商品主图URL */
    private String image;

    /** 商品价格 */
    private BigDecimal price;

    /** 库存数量 */
    private Integer stock;

    /** 上架状态: 0-下架 1-上架 */
    private Integer onSale;

    /** 商品评分 */
    private BigDecimal rating;
}
