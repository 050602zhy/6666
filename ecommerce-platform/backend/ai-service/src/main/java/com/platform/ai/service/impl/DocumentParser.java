package com.platform.ai.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档解析工具
 * 支持 PDF、TXT 格式
 */
@Slf4j
@Service
public class DocumentParser {

    /**
     * 解析文档获取文本
     */
    public String parse(String filePath, String fileType) {
        try {
            switch (fileType.toLowerCase()) {
                case "pdf":
                    return parsePdf(filePath);
                case "txt":
                case "text":
                    return parseTxt(filePath);
                default:
                    log.warn("不支持的文件类型: {}", fileType);
                    return parseTxt(filePath);
            }
        } catch (Exception e) {
            log.error("解析文档失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 解析PDF（限制最大100页，防止OOM）
     */
    private String parsePdf(String filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            int totalPages = document.getNumberOfPages();
            int maxPages = Math.min(totalPages, 100);
            if (totalPages > 100) {
                log.warn("PDF共{}页，仅解析前100页", totalPages);
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(maxPages);
            return stripper.getText(document);
        }
    }

    /**
     * 解析TXT
     */
    private String parseTxt(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    /**
     * 文本分块
     * @param text 原始文本
     * @param chunkSize 每块最大字符数
     * @param overlap 块之间重叠字符数
     */
    public List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // 清理文本
        text = text.replaceAll("\\s+", " ").trim();

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());

            // 尽量在句子边界分块
            if (end < text.length()) {
                int lastPeriod = text.lastIndexOf("。", end);
                int lastQuestion = text.lastIndexOf("？", end);
                int lastExclaim = text.lastIndexOf("！", end);
                int lastNewline = text.lastIndexOf("\n", end);

                int boundary = Math.max(lastPeriod, Math.max(lastQuestion, Math.max(lastExclaim, lastNewline)));
                if (boundary > start) {
                    end = boundary + 1;
                }
            }

            chunks.add(text.substring(start, end).trim());
            if (end >= text.length()) {
                break; // 已处理到最后
            }
            start = end - overlap;
            if (start < 0) start = 0;
            // 防止无限循环：确保start至少前进1个字符
            if (start >= end) {
                start = end;
            }
        }

        return chunks;
    }

    /**
     * 文本分块（默认重叠50字符）
     */
    public List<String> chunkText(String text, int chunkSize) {
        return chunkText(text, chunkSize, 50);
    }
}