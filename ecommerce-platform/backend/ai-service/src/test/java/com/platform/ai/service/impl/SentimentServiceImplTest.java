package com.platform.ai.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 情绪分析服务单元测试
 *
 * 测试用例清单：
 * 1. TC-SENT-001: 正面文本分析，返回1
 * 2. TC-SENT-002: 负面文本分析，返回3
 * 3. TC-SENT-003: 中性文本分析，返回2
 * 4. TC-SENT-004: 空文本分析，返回2
 * 5. TC-SENT-005: 混合情感文本分析
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("情绪分析服务单元测试")
class SentimentServiceImplTest {

    @InjectMocks
    private SentimentServiceImpl sentimentService;

    @Test
    @DisplayName("TC-SENT-001: 正面文本-满意")
    void analyze_PositiveText_Return1() {
        int result = sentimentService.analyze("非常满意，质量很好！");
        assertEquals(1, result, "正面文本应返回1");
    }

    @Test
    @DisplayName("TC-SENT-002: 负面文本-差")
    void analyze_NegativeText_Return3() {
        int result = sentimentService.analyze("太差了，非常失望！");
        assertEquals(3, result, "负面文本应返回3");
    }

    @Test
    @DisplayName("TC-SENT-003: 中性文本")
    void analyze_NeutralText_Return2() {
        int result = sentimentService.analyze("这是一般的产品");
        assertEquals(2, result, "中性文本应返回2");
    }

    @Test
    @DisplayName("TC-SENT-004: 空文本")
    void analyze_EmptyText_Return2() {
        int result = sentimentService.analyze("");
        assertEquals(2, result, "空文本应返回2");
    }

    @Test
    @DisplayName("TC-SENT-005: 混合情感文本-负面词多于正面")
    void analyze_MixedText_NegativeDominant_Return3() {
        int result = sentimentService.analyze("虽然价格便宜，但是质量太差很失望");
        assertEquals(3, result, "负面词多于正面应返回3");
    }
}
