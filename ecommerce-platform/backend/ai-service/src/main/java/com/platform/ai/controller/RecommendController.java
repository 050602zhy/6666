package com.platform.ai.controller;

import com.platform.common.result.Result;
import com.platform.ai.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 智能推荐控制器
 */
@RestController
@RequestMapping("/ai/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping
    public Result<String> recommend(@RequestParam Long userId) {
        return Result.success(recommendService.recommend(userId));
    }
}
