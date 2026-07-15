package com.platform.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.biz.entity.Activity;
import com.platform.biz.entity.ActivityProduct;
import com.platform.biz.entity.Order;
import com.platform.biz.entity.Product;
import com.platform.biz.entity.ProductReview;
import com.platform.biz.entity.StockLock;
import com.platform.biz.entity.User;
import com.platform.biz.entity.UserMessage;
import com.platform.biz.entity.VipConfig;
import com.platform.biz.mapper.ActivityMapper;
import com.platform.biz.mapper.ActivityProductMapper;
import com.platform.biz.mapper.OrdersMapper;
import com.platform.biz.mapper.ProductMapper;
import com.platform.biz.mapper.ProductReviewMapper;
import com.platform.biz.mapper.StockLockMapper;
import com.platform.biz.mapper.UserMapper;
import com.platform.biz.mapper.UserMessageMapper;
import com.platform.biz.mapper.VipConfigMapper;
import com.platform.biz.service.OrderService;
import com.platform.biz.service.ProductService;
import com.platform.biz.vo.ProductDetailVO;
import com.platform.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductReviewMapper productReviewMapper;
    private final StockLockMapper stockLockMapper;
    private final OrdersMapper ordersMapper;
    private final ActivityMapper activityMapper;
    private final ActivityProductMapper activityProductMapper;
    private final UserMessageMapper userMessageMapper;
    private final UserMapper userMapper;
    private final VipConfigMapper vipConfigMapper;
    private final OrderService orderService;

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

    @Override
    public List<Product> onSaleList() {
        log.info("查询上架商品列表");
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getOnSale, 1);
        wrapper.orderByAsc(Product::getId);
        List<Product> products = productMapper.selectList(wrapper);
        for (Product product : products) {
            fillDiscountInfo(product);
        }
        return products;
    }

    @Override
    public void save(Product product) {
        log.info("保存新商品: {}", product.getName());
        product.setOnSale(0);
        product.setRating(BigDecimal.ZERO);
        productMapper.insert(product);
    }

    @Override
    public List<Product> listBySellerId(Long sellerId) {
        log.info("查询卖家商品列表: sellerId={}", sellerId);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, sellerId);
        wrapper.orderByAsc(Product::getId);
        return productMapper.selectList(wrapper);
    }

    /**
     * 填充商品折扣信息（查找已发布活动中关联的折扣）
     */
    private void fillDiscountInfo(Product product) {
        if (product == null) {
            return;
        }
        LambdaQueryWrapper<ActivityProduct> apWrapper = new LambdaQueryWrapper<>();
        apWrapper.eq(ActivityProduct::getProductId, product.getId());
        List<ActivityProduct> aps = activityProductMapper.selectList(apWrapper);
        for (ActivityProduct ap : aps) {
            Activity activity = activityMapper.selectById(ap.getActivityId());
            if (activity != null && activity.getStatus() != null && activity.getStatus() == 1) {
                product.setDiscount(ap.getDiscount());
                product.setDiscountPrice(ap.getDiscountPrice());
                product.setOriginalPrice(ap.getOriginalPrice());
                return;
            }
        }
    }

    @Override
    public ProductDetailVO getProductDetail(Long id) {
        log.info("查询商品详情: {}", id);
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        LambdaQueryWrapper<ProductReview> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(ProductReview::getProductId, id);
        reviewWrapper.orderByDesc(ProductReview::getCreateTime);
        List<ProductReview> reviews = productReviewMapper.selectList(reviewWrapper);

        fillDiscountInfo(product);
        ProductDetailVO vo = new ProductDetailVO();
        vo.setProduct(product);
        vo.setReviews(reviews);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String buy(Long productId, Long buyerId, Integer quantity) {
        log.info("购买商品: productId={}, buyerId={}, quantity={}", productId, buyerId, quantity);
        if (quantity == null || quantity <= 0) {
            throw new BizException("购买数量必须大于0");
        }
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        if (product.getOnSale() == null || product.getOnSale() != 1) {
            throw new BizException("商品已下架");
        }

        // 检查可用库存 = 总库存 - 未过期锁定库存
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(15);
        LambdaQueryWrapper<StockLock> lockWrapper = new LambdaQueryWrapper<>();
        lockWrapper.eq(StockLock::getProductId, productId)
                .eq(StockLock::getStatus, 0)
                .ge(StockLock::getCreateTime, expireTime);
        List<StockLock> locks = stockLockMapper.selectList(lockWrapper);
        int lockedStock = locks.stream().mapToInt(StockLock::getQuantity).sum();

        if (product.getStock() - lockedStock < quantity) {
            throw new BizException("库存不足");
        }

        // 锁定库存
        StockLock stockLock = new StockLock();
        stockLock.setProductId(productId);
        stockLock.setUserId(buyerId);
        stockLock.setQuantity(quantity);
        stockLock.setStatus(0);
        stockLock.setCreateTime(LocalDateTime.now());
        stockLockMapper.insert(stockLock);

        // 生成订单号（从1000开始，每单+1）
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.orderByDesc(Order::getOrderNo);
        orderWrapper.last("LIMIT 1");
        Order lastOrder = ordersMapper.selectOne(orderWrapper);
        Integer newOrderNo = (lastOrder != null && lastOrder.getOrderNo() != null) ? lastOrder.getOrderNo() + 1 : 1000;

        // 计算订单金额（含VIP满减）
        BigDecimal originalAmount = product.getPrice().multiply(new BigDecimal(quantity));
        BigDecimal vipDiscount = BigDecimal.ZERO;

        // 查询买家VIP等级并计算满减
        User buyer = userMapper.selectById(buyerId);
        if (buyer != null && buyer.getVipLevel() != null && buyer.getVipLevel() > 0) {
            LambdaQueryWrapper<VipConfig> vipWrapper = new LambdaQueryWrapper<>();
            vipWrapper.eq(VipConfig::getLevel, buyer.getVipLevel());
            VipConfig vipConfig = vipConfigMapper.selectOne(vipWrapper);
            if (vipConfig != null && vipConfig.getThreshold() != null
                    && vipConfig.getThreshold() > 0
                    && vipConfig.getDiscountAmount() != null
                    && vipConfig.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                int times = originalAmount.intValue() / vipConfig.getThreshold();
                if (times > 0) {
                    vipDiscount = vipConfig.getDiscountAmount().multiply(new BigDecimal(times));
                }
            }
        }

        BigDecimal totalAmount = originalAmount.subtract(vipDiscount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // 扣减买家余额
        if (buyer == null) {
            throw new BizException("用户不存在");
        }
        if (buyer.getBalance().compareTo(totalAmount) < 0) {
            throw new BizException("余额不足，请先充值");
        }
        User updateBuyer = new User();
        updateBuyer.setId(buyerId);
        updateBuyer.setBalance(buyer.getBalance().subtract(totalAmount));
        userMapper.updateById(updateBuyer);

        // 创建订单
        Order order = new Order();
        order.setOrderNo(newOrderNo);
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setProductImage(product.getImage());
        order.setQuantity(quantity);
        order.setUnitPrice(product.getPrice());
        order.setTotalAmount(totalAmount);
        order.setVipDiscount(vipDiscount);
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setStatus(1); // 已付款待发货
        order.setIsRated(0);
        order.setRefundStatus(0);
        ordersMapper.insert(order);

        // 通知卖家：有新订单
        UserMessage msg = new UserMessage();
        msg.setUserId(product.getSellerId());
        msg.setSenderId(buyerId);
        msg.setType(4);
        msg.setTitle("新订单通知");
        msg.setContent("您有新的订单，商品「" + product.getName() + "」×" + quantity + "，订单号：" + newOrderNo + "，金额：¥" + order.getTotalAmount());
        msg.setIsRead(0);
        msg.setRelatedId(order.getId());
        userMessageMapper.insert(msg);

        // 自动发货：如果卖家开启了自动发货，立即执行发货
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null && seller.getAutoAcceptOrder() != null && seller.getAutoAcceptOrder() == 1) {
            try {
                orderService.ship(order.getId());
                log.info("自动发货成功: orderId={}", order.getId());
            } catch (Exception e) {
                log.warn("自动发货失败: orderId={}, reason={}", order.getId(), e.getMessage());
            }
        }

        return String.valueOf(newOrderNo);
    }

    @Override
    public void addComment(Long productId, Long orderId, Long userId, Integer rating, String content) {
        log.info("添加商品评论: productId={}, orderId={}, userId={}", productId, orderId, userId);
        if (rating == null || rating < 1 || rating > 5) {
            throw new BizException("评分必须在1~5之间");
        }
        ProductReview review = new ProductReview();
        review.setProductId(productId);
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setRating(rating);
        review.setContent(content);
        productReviewMapper.insert(review);
    }
}
