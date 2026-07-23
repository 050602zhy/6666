package com.platform.biz.service.impl;

import com.platform.biz.dto.ActivityDTO;
import com.platform.biz.entity.Activity;
import com.platform.biz.entity.ActivityProduct;
import com.platform.biz.entity.Product;
import com.platform.biz.mapper.ActivityMapper;
import com.platform.biz.mapper.ActivityProductMapper;
import com.platform.biz.mapper.ProductMapper;
import com.platform.biz.vo.ActivityDetailVO;
import com.platform.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 活动服务单元测试
 * 
 * 测试用例清单：
 * 1. TC-ACT-001: 创建活动-参数合法
 * 2. TC-ACT-002: 创建活动-折扣率超出范围
 * 3. TC-ACT-003: 查询活动列表
 * 4. TC-ACT-004: 发布活动-活动存在且是所有者
 * 5. TC-ACT-005: 发布活动-非所有者
 * 6. TC-ACT-006: 撤销发布活动
 * 7. TC-ACT-007: 查询活动详情
 * 8. TC-ACT-008: 删除活动
 * 9. TC-ACT-009: 删除活动-非所有者
 * 10. TC-ACT-010: 查询进行中活动商品-无活动
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("活动服务单元测试")
class ActivityServiceImplTest {

    @Mock
    private ActivityMapper activityMapper;
    @Mock
    private ActivityProductMapper activityProductMapper;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ActivityServiceImpl activityService;

    private Activity mockActivity;
    private ActivityProduct mockActivityProduct;

    @BeforeEach
    void setUp() {
        mockActivity = new Activity();
        mockActivity.setId(1L);
        mockActivity.setName("618大促");
        mockActivity.setDescription("年中大促活动");
        mockActivity.setStatus(0);
        mockActivity.setCreatorId(10L);
        mockActivity.setStartTime(LocalDateTime.now().minusDays(1));
        mockActivity.setEndTime(LocalDateTime.now().plusDays(7));

        mockActivityProduct = new ActivityProduct();
        mockActivityProduct.setId(1L);
        mockActivityProduct.setActivityId(1L);
        mockActivityProduct.setProductId(100L);
        mockActivityProduct.setDiscount(new BigDecimal("0.8"));
        mockActivityProduct.setDiscountPrice(new BigDecimal("80.00"));
        mockActivityProduct.setOriginalPrice(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("TC-ACT-001: 创建活动-参数合法")
    void create_ValidDTO_Success() {
        ActivityDTO dto = new ActivityDTO();
        dto.setName("测试活动");
        dto.setDescription("测试描述");
        dto.setCreatorId(10L);
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusDays(7));

        ActivityProduct ap = new ActivityProduct();
        ap.setProductId(100L);
        ap.setDiscount(new BigDecimal("0.8"));
        dto.setActivityProducts(Collections.singletonList(ap));

        when(activityMapper.insert(any(Activity.class))).thenAnswer(inv -> {
            Activity a = inv.getArgument(0);
            a.setId(1L);
            return 1;
        });
        when(activityProductMapper.insert(any(ActivityProduct.class))).thenReturn(1);

        activityService.create(dto);

        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityMapper).insert(activityCaptor.capture());
        assertEquals("测试活动", activityCaptor.getValue().getName());
        assertEquals(0, activityCaptor.getValue().getStatus());
        assertEquals(10L, activityCaptor.getValue().getCreatorId());

        verify(activityProductMapper).insert(any(ActivityProduct.class));
    }

    @Test
    @DisplayName("TC-ACT-002: 创建活动-折扣率超出范围")
    void create_InvalidDiscount_ThrowException() {
        ActivityDTO dto = new ActivityDTO();
        dto.setName("测试活动");
        dto.setCreatorId(10L);

        ActivityProduct ap = new ActivityProduct();
        ap.setProductId(100L);
        ap.setDiscount(new BigDecimal("0.05")); // 低于0.1
        dto.setActivityProducts(Collections.singletonList(ap));

        assertThrows(BizException.class, () -> activityService.create(dto));
        verify(activityMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-ACT-003: 查询活动列表")
    void list_ReturnActivities() {
        when(activityMapper.selectList(any())).thenReturn(Collections.singletonList(mockActivity));

        List<Activity> result = activityService.list(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("618大促", result.get(0).getName());
    }

    @Test
    @DisplayName("TC-ACT-004: 发布活动-所有者")
    void publish_Owner_Success() {
        when(activityMapper.selectById(1L)).thenReturn(mockActivity);
        when(activityProductMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(activityMapper.updateById(any(Activity.class))).thenReturn(1);

        activityService.publish(1L, 10L);

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("TC-ACT-005: 发布活动-非所有者")
    void publish_NotOwner_ThrowException() {
        when(activityMapper.selectById(1L)).thenReturn(mockActivity);

        assertThrows(BizException.class, () -> activityService.publish(1L, 99L));
        verify(activityMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("TC-ACT-006: 撤销发布活动")
    void unpublish_Success() {
        when(activityMapper.selectById(1L)).thenReturn(mockActivity);
        when(activityMapper.updateById(any(Activity.class))).thenReturn(1);

        activityService.unpublish(1L, 10L);

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityMapper).updateById(captor.capture());
        assertEquals(0, captor.getValue().getStatus());
    }

    @Test
    @DisplayName("TC-ACT-007: 查询活动详情")
    void getDetail_ReturnDetail() {
        when(activityMapper.selectById(1L)).thenReturn(mockActivity);
        when(activityProductMapper.selectList(any())).thenReturn(
                Collections.singletonList(mockActivityProduct));

        ActivityDetailVO result = activityService.getDetail(1L);

        assertNotNull(result);
        assertNotNull(result.getActivity());
        assertEquals("618大促", result.getActivity().getName());
        assertEquals(1, result.getActivityProducts().size());
    }

    @Test
    @DisplayName("TC-ACT-008: 删除活动-所有者")
    void delete_Owner_Success() {
        when(activityMapper.selectById(1L)).thenReturn(mockActivity);
        when(activityProductMapper.delete(any())).thenReturn(1);
        when(activityMapper.deleteById(1L)).thenReturn(1);

        activityService.delete(1L, 10L);

        verify(activityProductMapper).delete(any());
        verify(activityMapper).deleteById(1L);
    }

    @Test
    @DisplayName("TC-ACT-009: 删除活动-非所有者")
    void delete_NotOwner_ThrowException() {
        when(activityMapper.selectById(1L)).thenReturn(mockActivity);

        assertThrows(BizException.class, () -> activityService.delete(1L, 99L));
        verify(activityMapper, never()).deleteById(any());
    }

    @Test
    @DisplayName("TC-ACT-010: 查询进行中活动商品-无活动")
    void listActiveActivityProducts_NoActivities_ReturnEmpty() {
        when(activityMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<Product> result = activityService.listActiveActivityProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}