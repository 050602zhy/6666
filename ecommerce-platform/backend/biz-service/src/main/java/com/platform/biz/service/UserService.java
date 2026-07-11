package com.platform.biz.service;

import com.platform.biz.dto.RegisterDTO;
import com.platform.biz.vo.UserVO;

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
}