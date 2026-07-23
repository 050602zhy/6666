package com.platform.biz.service.impl;

import com.platform.biz.entity.Product;
import com.platform.biz.entity.User;
import com.platform.biz.mapper.ProductMapper;
import com.platform.biz.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 商品服务单元测试
 *
 * 测试用例清单：
 * 1. TC-PROD-001: 根据ID查询商品，商品存在，返回商品信息
 * 2. TC-PROD-002: 根据ID查询商品，商品不存在，返回null
 * 3. TC-PROD-003: 创建商品，参数合法，创建成功
 * 4. TC-PROD-004: 创建商品，名称为空，抛出异常
 * 5. TC-PROD-005: 商品列表查询，返回所有商品
 * 6. TC-PROD-006: 商品上架状态更新，上架成功
 * 7. TC-PROD-007: 填充卖家名称，sellerId存在，返回卖家昵称
 * 8. TC-PROD-008: 填充卖家名称，sellerId不存在，sellerName为null
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("商品服务单元测试")
class ProductServiceImplTest {

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product mockProduct;
    private User mockSeller;

    @BeforeEach
    void setUp() {
        mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("无线蓝牙耳机");
        mockProduct.setPrice(new BigDecimal("455.00"));
        mockProduct.setStock(149);
        mockProduct.setSellerId(5L);
        mockProduct.setOnSale(1);

        mockSeller = new User();
        mockSeller.setId(5L);
        mockSeller.setUsername("seller1");
        mockSeller.setNickname("卖家一号");
    }

    @Test
    @DisplayName("TC-PROD-001: 查询商品详情-商品存在")
    void getById_ProductExists_ReturnProduct() {
        when(productMapper.selectById(1L)).thenReturn(mockProduct);

        Product result = productService.getById(1L);

        assertNotNull(result);
        assertEquals("无线蓝牙耳机", result.getName());
        assertEquals(new BigDecimal("455.00"), result.getPrice());
        verify(productMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("TC-PROD-002: 查询商品详情-商品不存在")
    void getById_ProductNotExists_ReturnNull() {
        when(productMapper.selectById(999L)).thenReturn(null);

        Product result = productService.getById(999L);

        assertNull(result);
        verify(productMapper, times(1)).selectById(999L);
    }

    @Test
    @DisplayName("TC-PROD-003: 创建商品-参数合法")
    void save_ValidProduct_Success() {
        when(productMapper.insert(any(Product.class))).thenReturn(1);

        Product newProduct = new Product();
        newProduct.setName("测试商品");
        newProduct.setPrice(new BigDecimal("99.00"));
        newProduct.setStock(100);

        productService.save(newProduct);

        assertEquals(0, newProduct.getOnSale());
        assertEquals(BigDecimal.ZERO, newProduct.getRating());
        verify(productMapper, times(1)).insert(any(Product.class));
    }

    @Test
    @DisplayName("TC-PROD-005: 查询商品列表")
    void list_ReturnAllProducts() {
        java.util.List<Product> list = java.util.Collections.singletonList(mockProduct);
        when(productMapper.selectList(any())).thenReturn(list);

        java.util.List<Product> result = productService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("无线蓝牙耳机", result.get(0).getName());
    }

    @Test
    @DisplayName("TC-PROD-007: 填充卖家名称-sellerId存在")
    void fillSellerName_SellerExists_SetSellerName() {
        when(userMapper.selectById(5L)).thenReturn(mockSeller);

        java.lang.reflect.Method method;
        try {
            method = ProductServiceImpl.class.getDeclaredMethod("fillSellerName", Product.class);
            method.setAccessible(true);
            method.invoke(productService, mockProduct);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }

        assertEquals("卖家一号", mockProduct.getSellerName());
        verify(userMapper, times(1)).selectById(5L);
    }

    @Test
    @DisplayName("TC-PROD-008: 填充卖家名称-sellerId不存在")
    void fillSellerName_SellerNotExists_SellerNameNull() {
        mockProduct.setSellerId(999L);
        when(userMapper.selectById(999L)).thenReturn(null);

        java.lang.reflect.Method method;
        try {
            method = ProductServiceImpl.class.getDeclaredMethod("fillSellerName", Product.class);
            method.setAccessible(true);
            method.invoke(productService, mockProduct);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }

        assertNull(mockProduct.getSellerName());
    }
}
