package com.platform.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.biz.dto.RegisterDTO;
import com.platform.biz.entity.User;
import com.platform.biz.mapper.UserMapper;
import com.platform.biz.service.UserService;
import com.platform.biz.vo.UserVO;
import com.platform.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public UserVO login(String username, String password) {
        log.info("用户登录: {}", username);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getPassword, password);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BizException("用户名或密码错误");
        }
        return convertToVO(user);
    }

    @Override
    public void register(RegisterDTO dto) {
        log.info("用户注册: {}", dto.getUsername());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        Long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname());
        user.setRole(dto.getRole());
        user.setBalance(BigDecimal.ZERO);
        user.setVipLevel(1);
        user.setAutoAcceptOrder(0);
        userMapper.insert(user);
    }

    @Override
    public UserVO getUserInfo(Long id) {
        log.info("获取用户信息: {}", id);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public void updateUser(User user) {
        log.info("更新用户信息: {}", user.getId());
        if (user.getId() == null) {
            throw new BizException("用户ID不能为空");
        }
        User update = new User();
        update.setId(user.getId());
        update.setNickname(user.getNickname());
        update.setAvatar(user.getAvatar());
        userMapper.updateById(update);
    }

    @Override
    public void updateBalance(Long id, BigDecimal amount) {
        log.info("更新用户余额: id={}, amount={}", id, amount);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        BigDecimal newBalance = user.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("余额不足");
        }
        User update = new User();
        update.setId(id);
        update.setBalance(newBalance);
        userMapper.updateById(update);
    }

    @Override
    public void updateVipLevel(Long id, Integer vipLevel) {
        log.info("升级VIP等级: id={}, vipLevel={}", id, vipLevel);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (vipLevel <= user.getVipLevel()) {
            throw new BizException("VIP等级只能升级不能降级");
        }
        User update = new User();
        update.setId(id);
        update.setVipLevel(vipLevel);
        userMapper.updateById(update);
    }

    @Override
    public void updateAutoAccept(Long id, Integer autoAcceptOrder) {
        log.info("切换自动接单: id={}, autoAcceptOrder={}", id, autoAcceptOrder);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        User update = new User();
        update.setId(id);
        update.setAutoAcceptOrder(autoAcceptOrder);
        userMapper.updateById(update);
    }

    /**
     * User 转 UserVO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        vo.setBalance(user.getBalance());
        vo.setVipLevel(user.getVipLevel());
        vo.setPreference(user.getPreference());
        vo.setAutoAcceptOrder(user.getAutoAcceptOrder());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
