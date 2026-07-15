package com.platform.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.biz.entity.Order;
import com.platform.biz.entity.Product;
import com.platform.biz.entity.ProductReview;
import com.platform.biz.entity.Refund;
import com.platform.biz.entity.User;
import com.platform.biz.entity.UserMessage;
import com.platform.biz.mapper.OrdersMapper;
import com.platform.biz.mapper.ProductMapper;
import com.platform.biz.mapper.ProductReviewMapper;
import com.platform.biz.mapper.RefundMapper;
import com.platform.biz.mapper.UserMapper;
import com.platform.biz.mapper.UserMessageMapper;
import com.platform.biz.service.OrderService;
import com.platform.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrdersMapper ordersMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final RefundMapper refundMapper;
    private final ProductReviewMapper productReviewMapper;
    private final UserMessageMapper userMessageMapper;

    @Override
    public Long create(Object orderDTO) {
        log.info("创建订单: {}", orderDTO);
        // TODO: 实现订单创建逻辑
        return 0L;
    }

    @Override
    public Object getById(Long id) {
        log.info("查询订单: {}", id);
        return ordersMapper.selectById(id);
    }

    @Override
    public List<Order> list(Long userId, String role) {
        log.info("查询订单列表: userId={}, role={}", userId, role);
        LocalDateTime startTime = LocalDateTime.now().minusDays(90);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if ("buyer".equals(role)) {
            wrapper.eq(Order::getBuyerId, userId);
        } else if ("seller".equals(role)) {
            wrapper.eq(Order::getSellerId, userId);
        }
        wrapper.ge(Order::getCreateTime, startTime);
        wrapper.orderByDesc(Order::getCreateTime);
        List<Order> orders = ordersMapper.selectList(wrapper);

        // 填充买家和卖家的用户名、退款ID
        for (Order order : orders) {
            if (order.getBuyerId() != null) {
                User buyer = userMapper.selectById(order.getBuyerId());
                if (buyer != null) {
                    order.setBuyerName(buyer.getUsername());
                }
            }
            if (order.getSellerId() != null) {
                User seller = userMapper.selectById(order.getSellerId());
                if (seller != null) {
                    order.setSellerName(seller.getUsername());
                }
            }
            // 填充退款记录ID
            if (order.getRefundStatus() != null && order.getRefundStatus() > 0) {
                LambdaQueryWrapper<Refund> rWrapper = new LambdaQueryWrapper<>();
                rWrapper.eq(Refund::getOrderId, order.getId());
                rWrapper.orderByDesc(Refund::getCreateTime);
                rWrapper.last("LIMIT 1");
                Refund refund = refundMapper.selectOne(rWrapper);
                if (refund != null) {
                    order.setRefundId(refund.getId());
                }
            }
        }

        return orders;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long orderId) {
        log.info("卖家同意出货: orderId={}", orderId);
        Order order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BizException("订单状态不是已付款待发货");
        }

        Product product = productMapper.selectById(order.getProductId());
        if (product == null) {
            throw new BizException("商品不存在");
        }

        User seller = userMapper.selectById(order.getSellerId());
        User buyer = userMapper.selectById(order.getBuyerId());
        if (seller == null || buyer == null) {
            throw new BizException("用户不存在");
        }

        // 减少商品库存
        if (product.getStock() < order.getQuantity()) {
            throw new BizException("商品库存不足");
        }
        Product updateProduct = new Product();
        updateProduct.setId(product.getId());
        updateProduct.setStock(product.getStock() - order.getQuantity());
        productMapper.updateById(updateProduct);

        // 增加卖家余额（货款到账）
        User updateSeller = new User();
        updateSeller.setId(seller.getId());
        updateSeller.setBalance(seller.getBalance().add(order.getTotalAmount()));
        userMapper.updateById(updateSeller);

        // 更新订单状态为已发货
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setStatus(2);
        ordersMapper.updateById(updateOrder);

        // 通知买家：卖家已发货
        sendMessage(order.getBuyerId(), order.getSellerId(), 4,
                "订单已发货",
                "您购买的商品「" + order.getProductName() + "」已发货，订单号：" + order.getOrderNo(),
                orderId);
    }

    @Override
    public void complete(Long orderId) {
        log.info("买家确认收货: orderId={}", orderId);
        Order order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BizException("订单状态不是已发货");
        }
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setStatus(3);
        ordersMapper.updateById(updateOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long orderId, String reason) {
        log.info("申请退货: orderId={}, reason={}", orderId, reason);
        Order order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getIsRated() != null && order.getIsRated() == 1) {
            throw new BizException("已评价的订单不可退货");
        }
        if (order.getRefundStatus() != null && order.getRefundStatus() == 1) {
            throw new BizException("订单正在退款中");
        }
        if (order.getRefundStatus() != null && order.getRefundStatus() == 2) {
            throw new BizException("订单已退款成功");
        }
        if (order.getStatus() == 3) {
            throw new BizException("已完成的订单不可退货");
        }

        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setBuyerId(order.getBuyerId());
        refund.setSellerId(order.getSellerId());
        refund.setReason(reason);
        refund.setStatus(0);
        refundMapper.insert(refund);

        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setRefundStatus(1);
        ordersMapper.updateById(updateOrder);

        // 通知卖家：买家申请退货
        User buyer = userMapper.selectById(order.getBuyerId());
        String buyerName = buyer != null ? buyer.getUsername() : "买家";
        sendMessage(order.getSellerId(), order.getBuyerId(), 1,
                "退货申请",
                buyerName + " 对订单「" + order.getProductName() + "」（订单号：" + order.getOrderNo() + "）发起了退货申请，原因：" + reason,
                orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefund(Long refundId, Integer status, String reply) {
        log.info("处理退货: refundId={}, status={}", refundId, status);
        Refund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new BizException("退款记录不存在");
        }
        if (status != 1 && status != 2) {
            throw new BizException("处理状态只能是1（同意）或2（拒绝）");
        }

        refund.setStatus(status);
        refund.setReply(reply);
        refundMapper.updateById(refund);

        Order order = ordersMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new BizException("订单不存在");
        }

        if (status == 1) {
            // 同意退款：恢复库存、恢复买家余额、减少卖家余额
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                Product updateProduct = new Product();
                updateProduct.setId(product.getId());
                updateProduct.setStock(product.getStock() + order.getQuantity());
                productMapper.updateById(updateProduct);
            }

            User seller = userMapper.selectById(order.getSellerId());
            User buyer = userMapper.selectById(order.getBuyerId());
            if (seller != null) {
                User updateSeller = new User();
                updateSeller.setId(seller.getId());
                updateSeller.setBalance(seller.getBalance().subtract(order.getTotalAmount()));
                userMapper.updateById(updateSeller);
            }
            if (buyer != null) {
                User updateBuyer = new User();
                updateBuyer.setId(buyer.getId());
                updateBuyer.setBalance(buyer.getBalance().add(order.getTotalAmount()));
                userMapper.updateById(updateBuyer);
            }

            Order updateOrder = new Order();
            updateOrder.setId(order.getId());
            updateOrder.setRefundStatus(2);
            ordersMapper.updateById(updateOrder);

            // 通知买家：退货已同意
            sendMessage(order.getBuyerId(), order.getSellerId(), 2,
                    "退货已同意",
                    "您对商品「" + order.getProductName() + "」的退货申请已通过，退款金额 ¥" + order.getTotalAmount() + " 已退回您的余额",
                    order.getId());
        } else {
            // 拒绝退款：恢复评分权限（即设置退款状态为拒绝，允许后续评分）
            Order updateOrder = new Order();
            updateOrder.setId(order.getId());
            updateOrder.setRefundStatus(3);
            ordersMapper.updateById(updateOrder);

            // 通知买家：退货已拒绝
            String replyText = reply != null ? "，回复：" + reply : "";
            sendMessage(order.getBuyerId(), order.getSellerId(), 2,
                    "退货已拒绝",
                    "您对商品「" + order.getProductName() + "」的退货申请已被拒绝" + replyText,
                    order.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rate(Long orderId, Integer rating, String content) {
        log.info("订单评分: orderId={}, rating={}", orderId, rating);
        Order order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getIsRated() != null && order.getIsRated() == 1) {
            throw new BizException("订单已评价");
        }
        if (order.getRefundStatus() != null && order.getRefundStatus() == 2) {
            throw new BizException("退款成功的订单不可评分");
        }
        if (order.getStatus() != 2 && order.getStatus() != 3) {
            throw new BizException("订单状态不可评价");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new BizException("评分必须在1~5之间");
        }

        // 创建评论
        ProductReview review = new ProductReview();
        review.setProductId(order.getProductId());
        review.setOrderId(orderId);
        review.setUserId(order.getBuyerId());
        review.setRating(rating);
        review.setContent(content);
        productReviewMapper.insert(review);

        // 更新订单为已评价
        Order updateOrder = new Order();
        updateOrder.setId(orderId);
        updateOrder.setIsRated(1);
        ordersMapper.updateById(updateOrder);

        // 更新商品评分（平均分）
        LambdaQueryWrapper<ProductReview> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(ProductReview::getProductId, order.getProductId());
        List<ProductReview> reviews = productReviewMapper.selectList(reviewWrapper);
        if (!reviews.isEmpty()) {
            BigDecimal avgRating = reviews.stream()
                    .map(r -> new BigDecimal(r.getRating()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(reviews.size()), 2, RoundingMode.HALF_UP);
            Product updateProduct = new Product();
            updateProduct.setId(order.getProductId());
            updateProduct.setRating(avgRating);
            productMapper.updateById(updateProduct);
        }
    }

    /** 发送消息给用户 */
    private void sendMessage(Long userId, Long senderId, Integer type, String title, String content, Long relatedId) {
        UserMessage msg = new UserMessage();
        msg.setUserId(userId);
        msg.setSenderId(senderId);
        msg.setType(type);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setRelatedId(relatedId);
        userMessageMapper.insert(msg);
    }
}
