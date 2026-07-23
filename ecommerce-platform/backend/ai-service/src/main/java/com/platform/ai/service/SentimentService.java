package com.platform.ai.service;

/**
 * 情绪分析服务
 */
public interface SentimentService {

    /**
     * 分析文本情绪
     * @param text 待分析文本
     * @return 1-正面，2-中性，3-负面
     */
    int analyze(String text);
}
