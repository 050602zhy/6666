package com.platform.biz.service;

import com.platform.biz.dto.RegisterDTO;
import com.platform.biz.entity.User;
import com.platform.biz.vo.UserVO;

import java.math.BigDecimal;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息（不含密码）
     */
    UserVO login(String username, String password);

    /**
     * 用户注册
     *
     * @param dto 注册信息
     */
    void register(RegisterDTO dto);

    /**
     * 获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    UserVO getUserInfo(Long id);

    /**
     * 更新用户信息（昵称、头像）
     *
     * @param user 用户信息
     */
    void updateUser(User user);

    /**
     * 更新用户余额
     *
     * @param id     用户ID
     * @param amount 变动金额（正数为加，负数为减）
     */
    void updateBalance(Long id, BigDecimal amount);

    /**
     * 升级VIP等级（只升不降）
     *
     * @param id       用户ID
     * @param vipLevel VIP等级
     */
    void updateVipLevel(Long id, Integer vipLevel);

    /**
     * 切换自动接单状态
     *
     * @param id               用户ID
     * @param autoAcceptOrder  自动接单状态: 0-否 1-是
     */
    void updateAutoAccept(Long id, Integer autoAcceptOrder);

    /**
     * 切换个性化推荐状态
     *
     * @param id                     用户ID
     * @param personalizedRecommend  个性化推荐状态: 0-否 1-是
     */
    void updatePersonalizedRecommend(Long id, Integer personalizedRecommend);
}
