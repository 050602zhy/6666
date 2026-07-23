-- RAG 知识库相关表

-- 知识库表
CREATE TABLE IF NOT EXISTS kb_knowledge_base (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '知识库名称',
    description VARCHAR(500) COMMENT '描述',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    type INT DEFAULT 1 COMMENT '类型：1-商品知识库 2-平台知识库',
    status INT DEFAULT 1 COMMENT '状态：0-构建中 1-可用 2-失败',
    doc_count INT DEFAULT 0 COMMENT '文档数量',
    chunk_count INT DEFAULT 0 COMMENT '文本块总数',
    collection_name VARCHAR(100) COMMENT 'Chroma集合名称',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 知识库文档表
CREATE TABLE IF NOT EXISTS kb_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    kb_id BIGINT NOT NULL COMMENT '所属知识库ID',
    name VARCHAR(200) NOT NULL COMMENT '文档名称',
    file_type VARCHAR(20) COMMENT '文件类型',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_path VARCHAR(500) COMMENT '存储路径',
    chunk_count INT DEFAULT 0 COMMENT '文本块数量',
    status INT DEFAULT 0 COMMENT '状态：0-处理中 1-完成 2-失败',
    error_msg VARCHAR(500) COMMENT '错误信息',
    deleted INT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 已有表结构升级（如果表已存在）
ALTER TABLE kb_knowledge_base ADD COLUMN IF NOT EXISTS collection_name VARCHAR(100) COMMENT 'Chroma集合名称';
ALTER TABLE kb_document ADD COLUMN IF NOT EXISTS error_msg VARCHAR(500) COMMENT '错误信息';
