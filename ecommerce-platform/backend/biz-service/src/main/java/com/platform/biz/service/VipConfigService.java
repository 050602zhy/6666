package com.platform.biz.service;

import com.platform.biz.entity.VipConfig;

import java.util.List;

/**
 * VIP配置服务接口
 */
public interface VipConfigService {

    /**
     * 获取VIP配置列表
     *
     * @return VIP配置列表
     */
    List<VipConfig> list();
}
