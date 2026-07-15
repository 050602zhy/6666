package com.platform.biz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO（不包含密码）
 */
@Data
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "角色", example = "seller")
    private String role;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "账户余额", example = "10000.00")
    private java.math.BigDecimal balance;

    @Schema(description = "VIP等级", example = "0")
    private Integer vipLevel;

    @Schema(description = "购物偏好")
    private String preference;

    @Schema(description = "是否自动接单: 0-否 1-是", example = "0")
    private Integer autoAcceptOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}