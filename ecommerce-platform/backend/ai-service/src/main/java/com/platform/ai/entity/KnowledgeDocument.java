package com.platform.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库文档
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_document")
public class KnowledgeDocument extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属知识库ID */
    private Long kbId;

    /** 文档名称 */
    private String name;

    /** 文件类型：pdf/txt/doc */
    private String fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 存储路径 */
    private String filePath;

    /** 文本块数量 */
    private Integer chunkCount;

    /** 状态：0-处理中 1-完成 2-失败 */
    private Integer status;

    /** 错误信息 */
    private String errorMsg;
}