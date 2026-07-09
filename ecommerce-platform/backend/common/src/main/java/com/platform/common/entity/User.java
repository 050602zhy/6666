package com.platform.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
@Schema(description = "用户实体")
public class User extends BaseEntity {

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "密码")
    @TableField("password")
    private String password;

    @Schema(description = "头像")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "角色：0-买家，1-卖家")
    @TableField("role")
    private Integer role;

    @Schema(description = "VIP等级")
    @TableField("vip_level")
    private Integer vipLevel;

    @Schema(description = "账户余额")
    @TableField("balance")
    private BigDecimal balance;

    @Schema(description = "用户偏好")
    @TableField("preference")
    private String preference;

    @Schema(description = "是否开启个性化推荐：0-否，1-是")
    @TableField("personalized_recommend")
    private Integer personalizedRecommend;

    @Schema(description = "是否自动接单：0-否，1-是")
    @TableField("auto_accept_order")
    private Integer autoAcceptOrder;
}
