package com.platform.biz.service;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 根据ID查询商品
     */
    Object getById(Long id);

    /**
     * 查询商品列表
     */
    List<Object> list();
}
