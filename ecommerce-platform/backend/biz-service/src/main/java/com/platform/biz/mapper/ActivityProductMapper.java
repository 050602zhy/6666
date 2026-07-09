package com.platform.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.biz.entity.ActivityProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动商品折扣关联 Mapper 接口
 */
@Mapper
public interface ActivityProductMapper extends BaseMapper<ActivityProduct> {
}
