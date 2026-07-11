package com.platform.biz.controller;

import com.platform.biz.dto.LoginDTO;
import com.platform.biz.dto.RegisterDTO;
import com.platform.biz.service.UserService;
import com.platform.biz.vo.UserVO;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/biz/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户登录注册接口")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据用户名和密码进行登录")
    public Result<UserVO> login(@RequestBody LoginDTO loginDTO) {
        UserVO userVO = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return Result.success(userVO);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账号")
    public Result<Void> register(@RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }
}