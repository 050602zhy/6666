package com.platform.biz.controller;

import com.platform.biz.entity.Product;
import com.platform.biz.service.ProductService;
import com.platform.biz.vo.ProductDetailVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 商品控制器集成测试
 *
 * 测试用例清单：
 * 1. TC-PROD-API-001: GET /biz/product/list 获取商品列表，返回200和商品数据
 * 2. TC-PROD-API-002: GET /biz/product/detail/{id} 获取商品详情，商品存在
 * 3. TC-PROD-API-003: GET /biz/product/detail/{id} 获取商品详情，商品不存在
 */
@WebMvcTest(controllers = ProductController.class)
@ContextConfiguration(classes = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("商品控制器集成测试")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("TC-PROD-API-001: 获取商品列表")
    void list_ReturnProductList() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("测试商品");
        product.setPrice(new BigDecimal("100.00"));

        when(productService.list()).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/biz/product/list")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].name").value("测试商品"));

        verify(productService, times(1)).list();
    }

    @Test
    @DisplayName("TC-PROD-API-002: 获取商品详情-存在")
    void getProductDetail_ProductExists() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("无线蓝牙耳机");
        product.setPrice(new BigDecimal("455.00"));
        product.setStock(149);
        product.setSellerName("seller1");

        ProductDetailVO vo = new ProductDetailVO();
        vo.setProduct(product);

        when(productService.getProductDetail(1L)).thenReturn(vo);

        mockMvc.perform(get("/biz/product/detail/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.product.name").value("无线蓝牙耳机"))
            .andExpect(jsonPath("$.data.product.sellerName").value("seller1"));

        verify(productService, times(1)).getProductDetail(1L);
    }

    @Test
    @DisplayName("TC-PROD-API-003: 获取商品详情-不存在")
    void getProductDetail_ProductNotExists() throws Exception {
        when(productService.getProductDetail(999L)).thenReturn(null);

        mockMvc.perform(get("/biz/product/detail/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isEmpty());
    }
}
