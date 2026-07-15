package com.platform.biz.service;

import com.platform.biz.entity.Order;

import java.util.List;

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

    /**
     * 查询订单列表（90天内）
     *
     * @param userId 用户ID
     * @param role   角色: buyer-买家 seller-卖家
     * @return 订单列表
     */
    List<Order> list(Long userId, String role);

    /**
     * 卖家同意出货
     *
     * @param orderId 订单ID
     */
    void ship(Long orderId);

    /**
     * 买家确认收货
     *
     * @param orderId 订单ID
     */
    void complete(Long orderId);

    /**
     * 申请退货
     *
     * @param orderId 订单ID
     * @param reason  退货原因
     */
    void refund(Long orderId, String reason);

    /**
     * 处理退货
     *
     * @param refundId 退款ID
     * @param status   处理状态: 1-同意 2-拒绝
     * @param reply    卖家回复
     */
    void handleRefund(Long refundId, Integer status, String reply);

    /**
     * 订单评分
     *
     * @param orderId 订单ID
     * @param rating  评分
     * @param content 评价内容
     */
    void rate(Long orderId, Integer rating, String content);
}
