package com.platform.ai.controller;

import com.platform.ai.entity.KnowledgeBase;
import com.platform.ai.entity.KnowledgeDocument;
import com.platform.ai.service.KnowledgeBaseService;
import com.platform.ai.service.RagService;
import com.platform.ai.vo.ChatResponseVO;
import com.platform.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库管理控制器
 */
@Tag(name = "知识库管理", description = "RAG知识库的创建、文档上传、问答")
@RestController
@RequestMapping("/ai/knowledge")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final RagService ragService;

    @PostMapping("/create")
    @Operation(summary = "创建知识库")
    public Result<KnowledgeBase> create(
            @Parameter(description = "知识库名称") @RequestParam(name = "name") String name,
            @Parameter(description = "描述") @RequestParam(name = "description", required = false) String description,
            @Parameter(description = "类型：1-商品知识库 2-平台知识库") @RequestParam(name = "type") Integer type,
            @Parameter(description = "创建者ID") @RequestParam(name = "creatorId") Long creatorId) {
        KnowledgeBase kb = knowledgeBaseService.createKnowledgeBase(name, description, type, creatorId);
        return Result.success(kb);
    }

    @GetMapping("/list")
    @Operation(summary = "获取知识库列表")
    public Result<List<KnowledgeBase>> list(
            @Parameter(description = "创建者ID") @RequestParam(name = "creatorId") Long creatorId) {
        return Result.success(knowledgeBaseService.listByCreator(creatorId));
    }

    @PostMapping("/{kbId}/upload")
    @Operation(summary = "上传文档到知识库")
    public Result<KnowledgeDocument> upload(
            @Parameter(description = "知识库ID") @PathVariable(name = "kbId") Long kbId,
            @RequestParam(name = "file") MultipartFile file) {
        return Result.success(knowledgeBaseService.uploadDocument(kbId, file));
    }

    @GetMapping("/{kbId}/documents")
    @Operation(summary = "获取知识库下的文档列表")
    public Result<List<KnowledgeDocument>> documents(
            @Parameter(description = "知识库ID") @PathVariable(name = "kbId") Long kbId) {
        return Result.success(knowledgeBaseService.listDocuments(kbId));
    }

    @DeleteMapping("/{kbId}")
    @Operation(summary = "删除知识库")
    public Result<Void> delete(
            @Parameter(description = "知识库ID") @PathVariable(name = "kbId") Long kbId) {
        knowledgeBaseService.deleteKnowledgeBase(kbId);
        return Result.success();
    }

    @DeleteMapping("/doc/{docId}")
    @Operation(summary = "删除文档")
    public Result<Void> deleteDoc(
            @Parameter(description = "文档ID") @PathVariable(name = "docId") Long docId) {
        knowledgeBaseService.deleteDocument(docId);
        return Result.success();
    }

    @PostMapping("/{kbId}/chat")
    @Operation(summary = "基于知识库的RAG问答")
    public Result<ChatResponseVO> chat(
            @Parameter(description = "知识库ID") @PathVariable(name = "kbId") Long kbId,
            @Parameter(description = "用户问题") @RequestParam(name = "question") String question) {
        return Result.success(ragService.chat(kbId, question));
    }
}