package com.platform.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.ai.entity.KnowledgeBase;
import com.platform.ai.entity.KnowledgeDocument;
import com.platform.ai.mapper.KnowledgeBaseMapper;
import com.platform.ai.mapper.KnowledgeDocumentMapper;
import com.platform.ai.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * 知识库服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {

    private final KnowledgeDocumentMapper documentMapper;
    private final ChromaService chromaService;
    private final DocumentParser documentParser;

    @Value("${chroma.collection-prefix:ecommerce_kb_}")
    private String collectionPrefix;

    @Override
    public KnowledgeBase createKnowledgeBase(String name, String description, Integer type, Long creatorId) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setName(name);
        kb.setDescription(description);
        kb.setCreatorId(creatorId);
        kb.setType(type);
        kb.setStatus(1);
        kb.setDocCount(0);
        kb.setChunkCount(0);
        save(kb);
        // save后ID已生成，再设置collectionName
        kb.setCollectionName(collectionPrefix + kb.getId());
        updateById(kb);
        return kb;
    }

    @Override
    public List<KnowledgeBase> listByCreator(Long creatorId) {
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getCreatorId, creatorId)
               .orderByDesc(KnowledgeBase::getCreateTime);
        return list(wrapper);
    }

    @Override
    public KnowledgeDocument uploadDocument(Long kbId, MultipartFile file) {
        KnowledgeBase kb = getById(kbId);
        if (kb == null) {
            throw new RuntimeException("知识库不存在");
        }

        String originalName = file.getOriginalFilename();
        String ext = originalName != null ? originalName.substring(originalName.lastIndexOf(".") + 1) : "txt";
        String saveName = UUID.randomUUID() + "." + ext;

        // 保存文件到本地
        String uploadDir = System.getProperty("user.dir") + "/uploads/kb/" + kbId;
        Path uploadPath = Paths.get(uploadDir);
        try {
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(saveName);
            file.transferTo(filePath.toFile());

            // 保存文档记录
            KnowledgeDocument doc = new KnowledgeDocument();
            doc.setKbId(kbId);
            doc.setName(originalName);
            doc.setFileType(ext.toLowerCase());
            doc.setFileSize(file.getSize());
            doc.setFilePath(filePath.toString());
            doc.setStatus(0);
            doc.setChunkCount(0);
            documentMapper.insert(doc);

            // 异步解析文档并存储向量
            parseAndVectorize(doc, kbId);

            // 更新知识库文档数
            kb.setDocCount(kb.getDocCount() + 1);
            updateById(kb);

            return doc;
        } catch (IOException e) {
            log.error("文档保存失败", e);
            throw new RuntimeException("文档保存失败: " + e.getMessage());
        }
    }

    /**
     * 解析文档并存储向量（异步线程执行）
     */
    private void parseAndVectorize(KnowledgeDocument doc, Long kbId) {
        new Thread(() -> {
            log.info("[解析线程] 开始解析文档: {}, id={}", doc.getName(), doc.getId());
            try {
                Thread.sleep(500); // 等主线程事务提交
                // 1. 解析文档获取文本
                log.info("[解析线程] 正在解析文件: {}", doc.getFilePath());
                String text = documentParser.parse(doc.getFilePath(), doc.getFileType());
                log.info("[解析线程] 解析完成, 文本长度: {}", text == null ? 0 : text.length());
                if (text == null || text.isEmpty()) {
                    doc.setStatus(2);
                    doc.setErrorMsg("文档内容为空或解析失败");
                    documentMapper.updateById(doc);
                    log.warn("[解析线程] 文档内容为空: {}", doc.getName());
                    return;
                }

                // 2. 文本分块
                List<String> chunks = documentParser.chunkText(text, 500);
                log.info("[解析线程] 文本分块完成, 块数: {}", chunks.size());

                // 3. 存储到Chroma
                String collectionName = collectionPrefix + kbId;
                log.info("[解析线程] 正在存储到Chroma集合: {}", collectionName);
                chromaService.addDocuments(collectionName, chunks, doc.getId(), doc.getName());
                log.info("[解析线程] Chroma存储完成");

                // 4. 更新文档状态
                doc.setStatus(1);
                doc.setChunkCount(chunks.size());
                documentMapper.updateById(doc);

                // 5. 更新知识库块数
                KnowledgeBase kb = getById(kbId);
                if (kb != null) {
                    kb.setChunkCount(kb.getChunkCount() + chunks.size());
                    updateById(kb);
                }

                log.info("[解析线程] 文档解析完成: {}, 分块数: {}", doc.getName(), chunks.size());
            } catch (Exception e) {
                log.error("[解析线程] 文档解析失败: {}", doc.getName(), e);
                try {
                    doc.setStatus(2);
                    doc.setErrorMsg(e.getMessage());
                    documentMapper.updateById(doc);
                } catch (Exception ex) {
                    log.error("[解析线程] 更新失败状态也失败了", ex);
                }
            }
        }).start();
    }

    @Override
    public List<KnowledgeDocument> listDocuments(Long kbId) {
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocument::getKbId, kbId)
               .orderByDesc(KnowledgeDocument::getCreateTime);
        return documentMapper.selectList(wrapper);
    }

    @Override
    public void deleteKnowledgeBase(Long kbId) {
        // 删除关联文档
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocument::getKbId, kbId);
        List<KnowledgeDocument> docs = documentMapper.selectList(wrapper);
        for (KnowledgeDocument doc : docs) {
            deleteDocumentFile(doc);
        }
        documentMapper.delete(wrapper);

        // 删除Chroma集合
        chromaService.deleteCollection(collectionPrefix + kbId);

        // 删除知识库
        removeById(kbId);
    }

    @Override
    public void deleteDocument(Long docId) {
        KnowledgeDocument doc = documentMapper.selectById(docId);
        if (doc == null) return;

        deleteDocumentFile(doc);

        // 更新知识库块数
        KnowledgeBase kb = getById(doc.getKbId());
        if (kb != null) {
            kb.setDocCount(Math.max(0, kb.getDocCount() - 1));
            kb.setChunkCount(Math.max(0, kb.getChunkCount() - (doc.getChunkCount() != null ? doc.getChunkCount() : 0)));
            updateById(kb);
        }

        documentMapper.deleteById(docId);
    }

    private void deleteDocumentFile(KnowledgeDocument doc) {
        try {
            if (doc.getFilePath() != null) {
                File file = new File(doc.getFilePath());
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            log.warn("删除文档文件失败: {}", doc.getFilePath());
        }
    }
}