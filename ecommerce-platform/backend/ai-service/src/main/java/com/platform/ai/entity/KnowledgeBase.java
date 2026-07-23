package com.platform.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_knowledge_base")
public class KnowledgeBase extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 知识库名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 创建者ID（卖家ID） */
    private Long creatorId;

    /** 类型：1-商品知识库 2-平台知识库 */
    private Integer type;

    /** 状态：0-构建中 1-可用 2-失败 */
    private Integer status;

    /** 文档数量 */
    private Integer docCount;

    /** 文本块总数 */
    private Integer chunkCount;

    /** Chroma集合名称 */
    private String collectionName;
}