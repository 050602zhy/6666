package com.platform.biz.service.impl;

import com.platform.biz.entity.Order;
import com.platform.biz.entity.Product;
import com.platform.biz.entity.Refund;
import com.platform.biz.entity.User;
import com.platform.biz.entity.UserMessage;
import com.platform.biz.mapper.OrdersMapper;
import com.platform.biz.mapper.ProductMapper;
import com.platform.biz.mapper.ProductReviewMapper;
import com.platform.biz.mapper.RefundMapper;
import com.platform.biz.mapper.UserMapper;
import com.platform.biz.mapper.UserMessageMapper;
import com.platform.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 订单服务单元测试
 *
 * 测试用例清单：
 * 1. TC-ORD-001: 查询订单-存在
 * 2. TC-ORD-002: 卖家发货-订单状态正确
 * 3. TC-ORD-003: 卖家发货-订单状态不是已付款
 * 4. TC-ORD-004: 确认收货-成功
 * 5. TC-ORD-005: 确认收货-状态不是已发货
 * 6. TC-ORD-006: 申请退货-成功
 * 7. TC-ORD-007: 申请退货-已评价不可退
 * 8. TC-ORD-008: 处理退货-同意退款
 * 9. TC-ORD-009: 处理退货-拒绝退款
 * 10. TC-ORD-010: 订单评分-成功
 * 11. TC-ORD-011: 订单评分-已评价
 * 12. TC-ORD-012: 订单评分-评分范围非法
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("订单服务单元测试")
class OrderServiceImplTest {

    @Mock
    private OrdersMapper ordersMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RefundMapper refundMapper;
    @Mock
    private ProductReviewMapper productReviewMapper;
    @Mock
    private UserMessageMapper userMessageMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order mockOrder;
    private Product mockProduct;
    private User mockBuyer;
    private User mockSeller;

    @BeforeEach
    void setUp() {
        mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setOrderNo(1001);
        mockOrder.setProductId(100L);
        mockOrder.setProductName("测试商品");
        mockOrder.setQuantity(1);
        mockOrder.setTotalAmount(new BigDecimal("100.00"));
        mockOrder.setBuyerId(1L);
        mockOrder.setSellerId(5L);
        mockOrder.setStatus(1); // 已付款待发货
        mockOrder.setIsRated(0);
        mockOrder.setRefundStatus(0);
        mockOrder.setRateExpireTime(LocalDateTime.now().plusHours(24));

        mockProduct = new Product();
        mockProduct.setId(100L);
        mockProduct.setName("测试商品");
        mockProduct.setStock(100);
        mockProduct.setPrice(new BigDecimal("100.00"));

        mockBuyer = new User();
        mockBuyer.setId(1L);
        mockBuyer.setUsername("buyer1");
        mockBuyer.setBalance(new BigDecimal("500.00"));

        mockSeller = new User();
        mockSeller.setId(5L);
        mockSeller.setUsername("seller1");
        mockSeller.setBalance(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("TC-ORD-001: 查询订单-存在")
    void getById_OrderExists_ReturnOrder() {
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder);

        Object result = orderService.getById(1L);

        assertNotNull(result);
        assertTrue(result instanceof Order);
    }

    @Test
    @DisplayName("TC-ORD-002: 卖家发货-订单状态正确")
    void ship_ValidStatus_Success() {
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder);
        when(productMapper.selectById(100L)).thenReturn(mockProduct);
        when(userMapper.selectById(1L)).thenReturn(mockBuyer);
        when(userMapper.selectById(5L)).thenReturn(mockSeller);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(ordersMapper.updateById(any(Order.class))).thenReturn(1);
        when(userMessageMapper.insert(any(UserMessage.class))).thenReturn(1);

        orderService.ship(1L);

        // 验证库存扣减
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateById(productCaptor.capture());
        assertEquals(99, productCaptor.getValue().getStock());

        // 验证订单状态更新为已发货
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(ordersMapper).updateById(orderCaptor.capture());
        assertEquals(2, orderCaptor.getValue().getStatus());
        assertNotNull(orderCaptor.getValue().getRateExpireTime());
    }

    @Test
    @DisplayName("TC-ORD-003: 卖家发货-订单状态不是已付款")
    void ship_InvalidStatus_ThrowException() {
        mockOrder.setStatus(2); // 已发货
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder);

        assertThrows(BizException.class, () -> orderService.ship(1L));
        verify(ordersMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("TC-ORD-004: 确认收货-成功")
    void complete_ValidStatus_Success() {
        mockOrder.setStatus(2); // 已发货
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder);
        when(ordersMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.complete(1L);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(ordersMapper).updateById(captor.capture());
        assertEquals(3, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("TC-ORD-005: 确认收货-状态不是已发货")
    void complete_InvalidStatus_ThrowException() {
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder); // status=1

        assertThrows(BizException.class, () -> orderService.complete(1L));
    }

    @Test
    @DisplayName("TC-ORD-006: 申请退货-成功")
    void refund_Valid_Success() {
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder);
        when(refundMapper.insert(any(Refund.class))).thenReturn(1);
        when(ordersMapper.updateById(any(Order.class))).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(mockBuyer);
        when(userMessageMapper.insert(any(UserMessage.class))).thenReturn(1);

        orderService.refund(1L, "质量问题");

        verify(refundMapper).insert(any(Refund.class));
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(ordersMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getRefundStatus());
    }

    @Test
    @DisplayName("TC-ORD-007: 申请退货-已评价不可退")
    void refund_AlreadyRated_ThrowException() {
        mockOrder.setIsRated(1);
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder);

        assertThrows(BizException.class, () -> orderService.refund(1L, "质量问题"));
        verify(refundMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-ORD-008: 处理退货-同意退款")
    void handleRefund_Approve_Success() {
        Refund refund = new Refund();
        refund.setId(1L);
        refund.setOrderId(1L);
        refund.setStatus(0);

        when(refundMapper.selectById(1L)).thenReturn(refund);
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder);
        when(productMapper.selectById(100L)).thenReturn(mockProduct);
        when(userMapper.selectById(1L)).thenReturn(mockBuyer);
        when(userMapper.selectById(5L)).thenReturn(mockSeller);
        when(refundMapper.updateById(any(Refund.class))).thenReturn(1);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(ordersMapper.updateById(any(Order.class))).thenReturn(1);
        when(userMessageMapper.insert(any(UserMessage.class))).thenReturn(1);

        orderService.handleRefund(1L, 1, "同意");

        verify(refundMapper).updateById(any(Refund.class));
        // 验证退款状态
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(ordersMapper).updateById(orderCaptor.capture());
        assertEquals(2, orderCaptor.getValue().getRefundStatus());
    }

    @Test
    @DisplayName("TC-ORD-009: 处理退货-拒绝退款")
    void handleRefund_Reject_Success() {
        Refund refund = new Refund();
        refund.setId(1L);
        refund.setOrderId(1L);
        refund.setStatus(0);

        when(refundMapper.selectById(1L)).thenReturn(refund);
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder);
        when(refundMapper.updateById(any(Refund.class))).thenReturn(1);
        when(ordersMapper.updateById(any(Order.class))).thenReturn(1);
        when(userMessageMapper.insert(any(UserMessage.class))).thenReturn(1);

        orderService.handleRefund(1L, 2, "不符合退货条件");

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(ordersMapper).updateById(orderCaptor.capture());
        assertEquals(3, orderCaptor.getValue().getRefundStatus());
    }

    @Test
    @DisplayName("TC-ORD-010: 订单评分-成功")
    void rate_Valid_Success() {
        mockOrder.setStatus(2); // 已发货
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder);
        when(productReviewMapper.insert(any())).thenReturn(1);
        when(ordersMapper.updateById(any(Order.class))).thenReturn(1);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);

        orderService.rate(1L, 5, "非常好");

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(ordersMapper).updateById(orderCaptor.capture());
        assertEquals(1, orderCaptor.getValue().getIsRated());
    }

    @Test
    @DisplayName("TC-ORD-011: 订单评分-已评价")
    void rate_AlreadyRated_ThrowException() {
        mockOrder.setIsRated(1);
        mockOrder.setStatus(2);
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder);

        assertThrows(BizException.class, () -> orderService.rate(1L, 5, "好"));
    }

    @Test
    @DisplayName("TC-ORD-012: 订单评分-评分范围非法")
    void rate_InvalidRating_ThrowException() {
        mockOrder.setStatus(2);
        when(ordersMapper.selectById(1L)).thenReturn(mockOrder);

        assertThrows(BizException.class, () -> orderService.rate(1L, 6, "超出范围"));
        verify(productReviewMapper, never()).insert(any());
    }
}