package com.platform.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * VIP等级配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vip_config")
@Schema(description = "VIP等级配置实体")
public class VipConfig extends BaseEntity {

    @Schema(description = "VIP等级")
    @TableField("level")
    private Integer level;

    @Schema(description = "等级名称")
    @TableField("name")
    private String name;

    @Schema(description = "升级阈值（累计消费金额）")
    @TableField("threshold")
    private Integer threshold;

    @Schema(description = "折扣金额")
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    @Schema(description = "等级描述")
    @TableField("description")
    private String description;
}
