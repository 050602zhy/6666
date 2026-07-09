package com.platform.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.biz.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
