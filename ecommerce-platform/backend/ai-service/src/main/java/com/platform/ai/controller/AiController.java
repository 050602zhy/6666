package com.platform.ai.controller;

import com.platform.ai.service.AiChatService;
import com.platform.ai.vo.ChatResponseVO;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 功能控制器
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI功能", description = "智能客服、评论分析、营销文案生成")
public class AiController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    @Operation(summary = "智能客服", description = "AI回答商品相关问题")
    public Result<ChatResponseVO> chat(
            @Parameter(description = "商品名称", required = true, example = "无线蓝牙耳机")
            @RequestParam(name = "productName") String productName,
            @Parameter(description = "商品描述")
            @RequestParam(name = "productDesc", required = false) String productDesc,
            @Parameter(description = "用户问题", required = true, example = "这个耳机音质怎么样？")
            @RequestParam(name = "question") String question) {
        ChatResponseVO vo = aiChatService.chatAboutProduct(productName, productDesc, question);
        return Result.success(vo);
    }

    @PostMapping("/analyzeComments")
    @Operation(summary = "评论情感分析", description = "AI分析商品评论并给出购买建议")
    public Result<ChatResponseVO> analyzeComments(
            @Parameter(description = "商品名称", required = true)
            @RequestParam(name = "productName") String productName,
            @Parameter(description = "评论内容列表", required = true)
            @RequestBody List<String> comments) {
        ChatResponseVO vo = aiChatService.analyzeComments(productName, comments);
        return Result.success(vo);
    }

    @PostMapping("/generateCopy")
    @Operation(summary = "生成营销文案", description = "AI根据描述生成活动名称、描述和改进建议")
    public Result<ChatResponseVO> generateCopy(
            @Parameter(description = "活动描述", required = true, example = "夏季清仓，面向大学生群体，降价T恤和短裤")
            @RequestParam(name = "description") String description) {
        ChatResponseVO vo = aiChatService.generateMarketingCopy(description);
        return Result.success(vo);
    }
}