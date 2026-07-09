package com.platform.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.biz.entity.ProductReview;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评论 Mapper 接口
 */
@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {
}
