package com.platform.biz.service.impl;

import com.platform.biz.dto.RegisterDTO;
import com.platform.biz.entity.User;
import com.platform.biz.mapper.UserMapper;
import com.platform.biz.vo.UserVO;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务单元测试
 *
 * 测试用例清单：
 * 1. TC-USER-001: 登录-用户名密码正确，返回用户信息
 * 2. TC-USER-002: 登录-用户名密码错误，抛出异常
 * 3. TC-USER-003: 注册-用户名不存在，注册成功
 * 4. TC-USER-004: 注册-用户名已存在，抛出异常
 * 5. TC-USER-005: 查询用户信息-用户存在
 * 6. TC-USER-006: 查询用户信息-用户不存在
 * 7. TC-USER-007: 更新用户信息-成功
 * 8. TC-USER-008: 更新余额-余额充足
 * 9. TC-USER-009: 更新余额-余额不足
 * 10. TC-USER-010: 升级VIP-合法升级
 * 11. TC-USER-011: 升级VIP-非法降级
 * 12. TC-USER-012: 切换自动接单
 * 13. TC-USER-013: 切换个性化推荐
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务单元测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("123456");
        mockUser.setNickname("测试用户");
        mockUser.setRole("buyer");
        mockUser.setBalance(new BigDecimal("1000.00"));
        mockUser.setVipLevel(2);
        mockUser.setAutoAcceptOrder(0);
        mockUser.setPersonalizedRecommend(1);
    }

    @Test
    @DisplayName("TC-USER-001: 登录-用户名密码正确")
    void login_ValidCredentials_ReturnUserVO() {
        when(userMapper.selectOne(any())).thenReturn(mockUser);

        UserVO result = userService.login("testuser", "123456");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getNickname());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
    }

    @Test
    @DisplayName("TC-USER-002: 登录-用户名密码错误")
    void login_InvalidCredentials_ThrowException() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThrows(BizException.class, () ->
                userService.login("wrong", "wrong"));
    }

    @Test
    @DisplayName("TC-USER-003: 注册-用户名不存在")
    void register_NewUsername_Success() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("123456");
        dto.setNickname("新用户");
        dto.setRole("buyer");

        when(userMapper.selectCount(any())).thenReturn(0L);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        userService.register(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User inserted = captor.getValue();
        assertEquals("newuser", inserted.getUsername());
        assertEquals(BigDecimal.ZERO, inserted.getBalance());
        assertEquals(1, inserted.getVipLevel());
        assertEquals(0, inserted.getAutoAcceptOrder());
    }

    @Test
    @DisplayName("TC-USER-004: 注册-用户名已存在")
    void register_ExistingUsername_ThrowException() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setPassword("123456");

        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BizException.class, () -> userService.register(dto));
        verify(userMapper, never()).insert(any());
    }

    @Test
    @DisplayName("TC-USER-005: 查询用户信息-存在")
    void getUserInfo_UserExists_ReturnUserVO() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        UserVO result = userService.getUserInfo(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("TC-USER-006: 查询用户信息-不存在")
    void getUserInfo_UserNotExists_ThrowException() {
        when(userMapper.selectById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> userService.getUserInfo(999L));
    }

    @Test
    @DisplayName("TC-USER-007: 更新用户信息-成功")
    void updateUser_ValidUser_Success() {
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        User update = new User();
        update.setId(1L);
        update.setNickname("新昵称");
        userService.updateUser(update);

        verify(userMapper).updateById(any(User.class));
    }

    @Test
    @DisplayName("TC-USER-008: 更新余额-余额充足")
    void updateBalance_SufficientBalance_Success() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.updateBalance(1L, new BigDecimal("500.00"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals(new BigDecimal("1500.00"), captor.getValue().getBalance());
    }

    @Test
    @DisplayName("TC-USER-009: 更新余额-余额不足")
    void updateBalance_InsufficientBalance_ThrowException() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        assertThrows(BizException.class, () ->
                userService.updateBalance(1L, new BigDecimal("-2000.00")));
        verify(userMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("TC-USER-010: 升级VIP-合法升级")
    void updateVipLevel_ValidUpgrade_Success() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.updateVipLevel(1L, 3);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals(3, captor.getValue().getVipLevel());
    }

    @Test
    @DisplayName("TC-USER-011: 升级VIP-非法降级")
    void updateVipLevel_Downgrade_ThrowException() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        assertThrows(BizException.class, () ->
                userService.updateVipLevel(1L, 1));
        verify(userMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("TC-USER-012: 切换自动接单")
    void updateAutoAccept_Success() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.updateAutoAccept(1L, 1);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getAutoAcceptOrder());
    }

    @Test
    @DisplayName("TC-USER-013: 切换个性化推荐")
    void updatePersonalizedRecommend_Success() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.updatePersonalizedRecommend(1L, 0);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertEquals(0, captor.getValue().getPersonalizedRecommend());
    }
}