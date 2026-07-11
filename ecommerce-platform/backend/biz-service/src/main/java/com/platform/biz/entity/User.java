package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    @Schema(description = "角色", example = "seller")
    private String role;
}