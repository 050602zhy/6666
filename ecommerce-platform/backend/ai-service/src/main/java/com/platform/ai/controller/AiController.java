package com.platform.ai.controller;

import com.platform.ai.dto.PlazaChatRequest;
import com.platform.ai.service.AiChatService;
import com.platform.ai.vo.ChatResponseVO;
import com.platform.ai.vo.MarketingCopyVO;
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
    @Operation(summary = "智能客服", description = "AI回答商品相关问题，基于前端传入的实时数据")
    public Result<ChatResponseVO> chat(
            @Parameter(description = "商品名称", required = true, example = "无线蓝牙耳机")
            @RequestParam(name = "productName") String productName,
            @Parameter(description = "商品描述")
            @RequestParam(name = "productDesc", required = false) String productDesc,
            @Parameter(description = "用户问题", required = true, example = "这个耳机音质怎么样？")
            @RequestParam(name = "question") String question,
            @Parameter(description = "商品ID")
            @RequestParam(name = "productId", required = false) Long productId,
            @Parameter(description = "用户ID")
            @RequestParam(name = "userId", required = false) Long userId,
            @Parameter(description = "用户余额")
            @RequestParam(name = "userBalance", required = false) Double userBalance,
            @Parameter(description = "用户VIP等级")
            @RequestParam(name = "userVipLevel", required = false) Integer userVipLevel,
            @Parameter(description = "商品实时数据JSON（price/stock/rating/discount/discountPrice/onSale）")
            @RequestParam(name = "productContext", required = false) String productContext) {
        ChatResponseVO vo = aiChatService.chatAboutProduct(productName, productDesc, question, productId, userId, userBalance, userVipLevel, productContext);
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
    @Operation(summary = "生成营销文案", description = "AI根据描述生成活动名称、描述和改进建议（结构化返回，支持一键填充）")
    public Result<MarketingCopyVO> generateCopy(
            @Parameter(description = "活动描述", required = true, example = "夏季清仓，面向大学生群体，降价T恤和短裤")
            @RequestParam(name = "description") String description) {
        MarketingCopyVO vo = aiChatService.generateMarketingCopy(description);
        return Result.success(vo);
    }

    @PostMapping("/plazaChat")
    @Operation(summary = "商品广场AI咨询", description = "AI导购助手回答关于所有商品的问题，支持推荐、比较、查询等")
    public Result<ChatResponseVO> plazaChat(@RequestBody PlazaChatRequest request) {
        ChatResponseVO vo = aiChatService.chatAboutPlaza(
                request.getQuestion(), request.getProductsJson(),
                request.getUserId(), request.getUserBalance(), request.getUserVipLevel());
        return Result.success(vo);
    }
}