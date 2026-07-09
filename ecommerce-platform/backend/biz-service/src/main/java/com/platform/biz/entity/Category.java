package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类实体
 * 对应表: category
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 分类名称 */
    private String name;

    /** 父分类ID */
    private Long parentId;

    /** 排序序号 */
    private Integer sortOrder;
}
