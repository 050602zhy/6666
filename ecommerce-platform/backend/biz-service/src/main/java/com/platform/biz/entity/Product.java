package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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

    /** 折扣率（非数据库字段，来自活动关联） */
    @TableField(exist = false)
    private BigDecimal discount;

    /** 折后价格（非数据库字段，来自活动关联） */
    @TableField(exist = false)
    private BigDecimal discountPrice;

    /** 原价（非数据库字段，来自活动关联） */
    @TableField(exist = false)
    private BigDecimal originalPrice;

    /** 卖家名称（非数据库字段，查询时填充） */
    @TableField(exist = false)
    private String sellerName;
}
