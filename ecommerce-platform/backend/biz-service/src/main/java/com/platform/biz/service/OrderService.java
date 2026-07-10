package com.platform.biz.service;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    Long create(Object orderDTO);

    /**
     * 根据ID查询订单
     */
    Object getById(Long id);
}
