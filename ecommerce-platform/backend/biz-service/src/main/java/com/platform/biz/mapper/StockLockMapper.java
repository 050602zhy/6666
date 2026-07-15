package com.platform.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.biz.entity.StockLock;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存锁定 Mapper 接口
 */
@Mapper
public interface StockLockMapper extends BaseMapper<StockLock> {
}
