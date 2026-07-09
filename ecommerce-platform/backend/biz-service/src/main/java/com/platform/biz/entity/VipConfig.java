package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * VIP等级配置实体
 * 对应表: vip_config
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vip_config")
public class VipConfig extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** VIP等级: 1~5 */
    private Integer level;

    /** 等级名称 */
    private String name;

    /** 升级门槛(消费金额/分) */
    private Integer threshold;

    /** 折扣金额 */
    private BigDecimal discountAmount;

    /** 等级描述 */
    private String description;
}
