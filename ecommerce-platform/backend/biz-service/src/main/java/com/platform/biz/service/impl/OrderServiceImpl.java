package com.platform.biz.service.impl;

import com.platform.biz.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 订单服务实现
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Override
    public Long create(Object orderDTO) {
        log.info("创建订单: {}", orderDTO);
        // TODO: 实现订单创建逻辑
        return 0L;
    }

    @Override
    public Object getById(Long id) {
        log.info("查询订单: {}", id);
        // TODO: 实现订单查询逻辑
        return null;
    }
}
