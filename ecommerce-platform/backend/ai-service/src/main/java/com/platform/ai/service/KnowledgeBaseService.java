package com.platform.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.ai.entity.KnowledgeBase;
import com.platform.ai.entity.KnowledgeDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库服务
 */
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    /**
     * 创建知识库
     */
    KnowledgeBase createKnowledgeBase(String name, String description, Integer type, Long creatorId);

    /**
     * 获取当前用户的知识库列表
     */
    List<KnowledgeBase> listByCreator(Long creatorId);

    /**
     * 上传文档到知识库
     */
    KnowledgeDocument uploadDocument(Long kbId, MultipartFile file);

    /**
     * 获取知识库下的文档列表
     */
    List<KnowledgeDocument> listDocuments(Long kbId);

    /**
     * 删除知识库（及关联文档）
     */
    void deleteKnowledgeBase(Long kbId);

    /**
     * 删除文档
     */
    void deleteDocument(Long docId);
}