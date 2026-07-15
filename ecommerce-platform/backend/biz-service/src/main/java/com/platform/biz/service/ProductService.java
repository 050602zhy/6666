package com.platform.biz.service;

import com.platform.biz.entity.Product;
import com.platform.biz.vo.ProductDetailVO;

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

    /**
     * 获取所有上架商品列表（按ID升序）
     *
     * @return 上架商品列表
     */
    List<Product> onSaleList();

    /**
     * 获取商品详情（含评论列表）
     *
     * @param id 商品ID
     * @return 商品详情
     */
    ProductDetailVO getProductDetail(Long id);

    /**
     * 购买商品（临时锁定库存并创建订单）
     *
     * @param productId 商品ID
     * @param buyerId   买家ID
     * @param quantity  购买数量
     * @return 订单号
     */
    String buy(Long productId, Long buyerId, Integer quantity);

    /**
     * 添加商品评论
     *
     * @param productId 商品ID
     * @param orderId   订单ID
     * @param userId    用户ID
     * @param rating    评分
     * @param content   评论内容
     */
    void addComment(Long productId, Long orderId, Long userId, Integer rating, String content);

    /**
     * 保存新商品
     *
     * @param product 商品信息
     */
    void save(Product product);

    /**
     * 查询指定卖家的商品列表
     *
     * @param sellerId 卖家ID
     * @return 商品列表
     */
    List<Product> listBySellerId(Long sellerId);
}
