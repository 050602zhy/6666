package com.platform.biz.controller;

import com.platform.biz.entity.UserMessage;
import com.platform.biz.service.MessageService;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息管理控制器
 */
@RestController
@RequestMapping("/biz/message")
@RequiredArgsConstructor
@Tag(name = "消息管理", description = "用户消息的查询、标记已读接口")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/list")
    @Operation(summary = "消息列表", description = "查询用户消息列表")
    public Result<List<UserMessage>> list(
            @Parameter(description = "用户ID", required = true, example = "1")
            @RequestParam(name = "userId") Long userId,
            @Parameter(description = "消息类型: all-全部 buyer-买家相关 seller-卖家相关 activity-活动", example = "all")
            @RequestParam(name = "type") String type) {
        return Result.success(messageService.list(userId, type));
    }

    @PutMapping("/read/{id}")
    @Operation(summary = "标记消息已读", description = "将消息标记为已读状态")
    public Result<Void> read(
            @Parameter(description = "消息ID", required = true, example = "1")
            @PathVariable Long id) {
        messageService.read(id);
        return Result.success();
    }
}
