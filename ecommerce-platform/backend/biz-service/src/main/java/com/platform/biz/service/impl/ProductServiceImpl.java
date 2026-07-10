package com.platform.biz.service.impl;

import com.platform.biz.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 商品服务实现
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Override
    public Object getById(Long id) {
        log.info("查询商品: {}", id);
        // TODO: 实现商品查询逻辑
        return null;
    }

    @Override
    public List<Object> list() {
        log.info("查询商品列表");
        // TODO: 实现商品列表查询逻辑
        return Collections.emptyList();
    }
}
