package com.platform.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存锁定实体
 * 对应表: stock_lock
 * 注意: 此类不继承BaseEntity，因为不需要deleted字段
 */
@Data
@TableName("stock_lock")
public class StockLock {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 用户ID */
    private Long userId;

    /** 锁定数量 */
    private Integer quantity;

    /** 锁定状态: 0-已锁定 1-已付款 2-已取消 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
