package com.platform.ai.service;

import com.platform.ai.vo.ChatResponseVO;

/**
 * RAG 检索增强生成服务
 */
public interface RagService {

    /**
     * 基于知识库的RAG问答
     *
     * @param kbId     知识库ID
     * @param question 用户问题
     * @return AI回复
     */
    ChatResponseVO chat(Long kbId, String question);
}