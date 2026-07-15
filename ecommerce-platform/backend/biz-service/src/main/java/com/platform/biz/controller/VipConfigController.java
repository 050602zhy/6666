package com.platform.biz.controller;

import com.platform.biz.entity.VipConfig;
import com.platform.biz.service.VipConfigService;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * VIP配置控制器
 */
@RestController
@RequestMapping("/biz/vip")
@RequiredArgsConstructor
@Tag(name = "VIP配置", description = "VIP等级配置查询接口")
public class VipConfigController {

    private final VipConfigService vipConfigService;

    @GetMapping("/config")
    @Operation(summary = "获取VIP配置列表", description = "查询所有VIP等级配置")
    public Result<List<VipConfig>> list() {
        return Result.success(vipConfigService.list());
    }
}
