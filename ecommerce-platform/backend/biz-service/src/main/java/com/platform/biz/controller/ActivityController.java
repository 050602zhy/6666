package com.platform.biz.controller;

import com.platform.biz.annotation.RequireRole;
import com.platform.biz.dto.ActivityDTO;
import com.platform.biz.entity.Activity;
import com.platform.biz.service.ActivityService;
import com.platform.biz.vo.ActivityDetailVO;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动管理控制器
 */
@RestController
@RequestMapping("/biz/activity")
@RequiredArgsConstructor
@Tag(name = "活动管理", description = "营销活动的创建、发布、查询接口")
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    @Operation(summary = "创建活动", description = "创建营销活动并关联折扣商品")
    @RequireRole("seller")
    public Result<Void> create(@RequestBody ActivityDTO dto) {
        activityService.create(dto);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "活动列表", description = "查询所有活动列表")
    public Result<List<Activity>> list() {
        return Result.success(activityService.list());
    }

    @PutMapping("/publish/{id}")
    @Operation(summary = "发布活动", description = "将活动状态改为已发布")
    @RequireRole("seller")
    public Result<Void> publish(
            @Parameter(description = "活动ID", required = true, example = "1")
            @PathVariable Long id) {
        activityService.publish(id);
        return Result.success();
    }

    @PutMapping("/unpublish/{id}")
    @Operation(summary = "撤销发布活动", description = "将活动状态改回草稿")
    @RequireRole("seller")
    public Result<Void> unpublish(
            @Parameter(description = "活动ID", required = true, example = "1")
            @PathVariable Long id) {
        activityService.unpublish(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除活动", description = "删除活动及其关联的折扣商品")
    @RequireRole("seller")
    public Result<Void> delete(
            @Parameter(description = "活动ID", required = true, example = "1")
            @PathVariable Long id) {
        activityService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "活动详情", description = "获取活动详情（含折扣商品）")
    public Result<ActivityDetailVO> getDetail(
            @Parameter(description = "活动ID", required = true, example = "1")
            @PathVariable Long id) {
        return Result.success(activityService.getDetail(id));
    }
}
