package com.platform.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.biz.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品 Mapper 接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}