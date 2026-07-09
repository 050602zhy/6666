package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
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
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String username;

    /** 密码(MD5加密) */
    private String password;

    /** 头像URL */
    private String avatar;

    /** 角色: 0-买家 1-卖家 */
    private Integer role;

    /** VIP等级: 0-普通 1~5 VIP等级 */
    private Integer vipLevel;

    /** 账户余额 */
    private BigDecimal balance;

    /** 购物偏好(逗号分隔) */
    private String preference;

    /** 个性化推荐: 0-关 1-开 */
    private Integer personalizedRecommend;

    /** 自动接单: 0-否 1-是 */
    private Integer autoAcceptOrder;
}
