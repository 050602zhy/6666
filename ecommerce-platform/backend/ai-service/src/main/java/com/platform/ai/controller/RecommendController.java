package com.platform.ai.controller;

import com.platform.ai.service.RecommendService;
import com.platform.ai.vo.RecommendProductVO;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能推荐控制器
 */
@RestController
@RequestMapping("/ai/recommend")
@RequiredArgsConstructor
// Knife4j/SpringDoc 接口分组标签，name 为显示名称，description 为详细描述
@Tag(name = "智能推荐", description = "基于用户行为的智能商品推荐接口")
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping
    // Operation 描述接口功能，summary 为简短摘要
    @Operation(summary = "获取推荐结果", description = "根据用户ID获取个性化商品推荐列表")
    public Result<List<RecommendProductVO>> recommend(
            // Parameter 描述参数含义
            @Parameter(description = "用户ID", required = true, example = "10001")
            @RequestParam Long userId) {
        return Result.success(recommendService.recommend(userId));
    }
}
