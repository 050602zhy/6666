package com.platform.biz.service;

import com.platform.biz.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 根据ID查询商品
     *
     * @param id 商品ID
     * @return 商品信息
     */
    Product getById(Long id);

    /**
     * 查询商品列表（按id升序）
     *
     * @return 商品列表
     */
    List<Product> list();

    /**
     * 更新商品信息（描述、价格、图片、上下架状态等）
     *
     * @param product 商品信息（只更新非null字段）
     */
    void updateProduct(Product product);

    /**
     * 更新商品图片
     *
     * @param id       商品ID
     * @param imageUrl 图片URL
     */
    void updateImage(Long id, String imageUrl);

    /**
     * 更新上下架状态
     *
     * @param id      商品ID
     * @param onSale  上架状态: 0-下架 1-上架
     */
    void updateOnSale(Long id, Integer onSale);
}