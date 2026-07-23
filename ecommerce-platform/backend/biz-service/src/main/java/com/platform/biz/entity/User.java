package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户实体
 * 对应表: user
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
@Schema(description = "用户实体")
public class User extends BaseEntity {

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "密码", hidden = true)
    private String password;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "头像(base64或URL)")
    private String avatar;

    @Schema(description = "角色", example = "seller")
    private String role;

    @Schema(description = "账户余额", example = "10000.00")
    private BigDecimal balance;

    @Schema(description = "VIP等级: 0-普通 1~5", example = "0")
    private Integer vipLevel;

    @Schema(description = "购物偏好(商品分类名称)")
    private String preference;

    @Schema(description = "是否开启个性化推荐: 0-否 1-是", example = "1")
    private Integer personalizedRecommend;

    @Schema(description = "是否自动接单: 0-否 1-是", example = "0")
    private Integer autoAcceptOrder;
}
