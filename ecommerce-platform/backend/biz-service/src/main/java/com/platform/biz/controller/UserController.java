package com.platform.biz.controller;

import com.platform.biz.dto.LoginDTO;
import com.platform.biz.dto.RegisterDTO;
import com.platform.biz.entity.User;
import com.platform.biz.service.UserService;
import com.platform.biz.vo.UserVO;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/biz/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户登录注册及信息接口")
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

    @GetMapping("/info/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息（含余额、VIP等级）")
    public Result<UserVO> getUserInfo(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户", description = "根据用户ID获取用户完整信息（偏好、个性化推荐设置等）")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }

    @PutMapping
    @Operation(summary = "更新用户信息", description = "更新用户昵称、头像等信息")
    public Result<Void> updateUser(@RequestBody User user) {
        userService.updateUser(user);
        return Result.success();
    }

    @PutMapping("/balance")
    @Operation(summary = "更新用户余额", description = "为用户加钱或扣钱（amount正数为加，负数为减）")
    public Result<Void> updateBalance(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        userService.updateBalance(id, amount);
        return Result.success();
    }

    @PutMapping("/vip")
    @Operation(summary = "升级VIP等级", description = "升级用户VIP等级（只升不降）")
    public Result<Void> updateVipLevel(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer vipLevel = Integer.valueOf(body.get("vipLevel").toString());
        userService.updateVipLevel(id, vipLevel);
        return Result.success();
    }

    @PutMapping("/autoAccept")
    @Operation(summary = "切换自动接单", description = "设置用户是否自动接单")
    public Result<Void> updateAutoAccept(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer autoAcceptOrder = Integer.valueOf(body.get("autoAcceptOrder").toString());
        userService.updateAutoAccept(id, autoAcceptOrder);
        return Result.success();
    }

    @PutMapping("/personalizedRecommend")
    @Operation(summary = "切换个性化推荐", description = "设置用户是否开启个性化推荐")
    public Result<Void> updatePersonalizedRecommend(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer personalizedRecommend = Integer.valueOf(body.get("personalizedRecommend").toString());
        userService.updatePersonalizedRecommend(id, personalizedRecommend);
        return Result.success();
    }
}
