package com.platform.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.biz.entity.Product;
import com.platform.biz.mapper.ProductMapper;
import com.platform.biz.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Product getById(Long id) {
        log.info("查询商品: {}", id);
        return productMapper.selectById(id);
    }

    @Override
    public List<Product> list() {
        log.info("查询商品列表");
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Product::getId);
        return productMapper.selectList(wrapper);
    }

    @Override
    public void updateProduct(Product product) {
        log.info("更新商品信息: {}", product.getId());
        productMapper.updateById(product);
    }

    @Override
    public void updateImage(Long id, String imageUrl) {
        log.info("更新商品图片: id={}, imageUrl={}", id, imageUrl);
        Product product = new Product();
        product.setId(id);
        product.setImage(imageUrl);
        productMapper.updateById(product);
    }

    @Override
    public void updateOnSale(Long id, Integer onSale) {
        log.info("更新商品上下架状态: id={}, onSale={}", id, onSale);
        Product product = new Product();
        product.setId(id);
        product.setOnSale(onSale);
        productMapper.updateById(product);
    }
}