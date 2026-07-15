package com.platform.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.biz.entity.VipConfig;
import com.platform.biz.mapper.VipConfigMapper;
import com.platform.biz.service.VipConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * VIP配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VipConfigServiceImpl implements VipConfigService {

    private final VipConfigMapper vipConfigMapper;

    @Override
    public List<VipConfig> list() {
        log.info("查询VIP配置列表");
        LambdaQueryWrapper<VipConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(VipConfig::getLevel);
        return vipConfigMapper.selectList(wrapper);
    }
}
