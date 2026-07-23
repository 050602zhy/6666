package com.platform.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chroma 向量数据库服务
 * 通过 HTTP REST API v2 调用 Chroma（需要显式提供 embeddings）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChromaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chroma.host:http://127.0.0.1}")
    private String chromaHost;

    @Value("${chroma.port:8000}")
    private int chromaPort;

    /** 向量维度 */
    private static final int EMBEDDING_DIM = 384;

    /** 集合名称 -> UUID 缓存 */
    private final Map<String, String> collectionIdCache = new ConcurrentHashMap<>();

    private String getBaseUrl() {
        return chromaHost + ":" + chromaPort;
    }

    /**
     * 生成简单文本嵌入（基于哈希，非语义嵌入，仅用于测试）
     * 生产环境应替换为调用真实嵌入模型 API
     */
    public List<Float> generateEmbedding(String text) {
        List<Float> embedding = new ArrayList<>(EMBEDDING_DIM);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < EMBEDDING_DIM; i++) {
                float val = ((hash[i % hash.length] & 0xFF) / 128.0f) - 1.0f;
                embedding.add(val);
            }
        } catch (Exception e) {
            for (int i = 0; i < EMBEDDING_DIM; i++) {
                embedding.add((float) (Math.random() * 2 - 1));
            }
        }
        return embedding;
    }

    /**
     * 获取或创建集合（v2 API），返回集合UUID
     */
    public String getOrCreateCollection(String name) {
        // 先查缓存
        String cachedId = collectionIdCache.get(name);
        if (cachedId != null) {
            return cachedId;
        }

        try {
            // 先查询所有集合，看是否已存在
            String listUrl = getBaseUrl() + "/api/v2/tenants/default_tenant/databases/default_database/collections";
            ResponseEntity<String> listResponse = restTemplate.getForEntity(listUrl, String.class);
            JsonNode collections = objectMapper.readTree(listResponse.getBody());
            for (JsonNode c : collections) {
                if (name.equals(c.path("name").asText())) {
                    String id = c.path("id").asText();
                    collectionIdCache.put(name, id);
                    log.debug("Chroma集合已存在: {} -> {}", name, id);
                    return id;
                }
            }

            // 不存在则创建
            String url = getBaseUrl() + "/api/v2/tenants/default_tenant/databases/default_database/collections";
            ObjectNode body = objectMapper.createObjectNode();
            body.put("name", name);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode created = objectMapper.readTree(response.getBody());
            String id = created.path("id").asText();
            collectionIdCache.put(name, id);
            log.info("Chroma集合已创建: {} -> {}", name, id);
            return id;
        } catch (Exception e) {
            log.error("创建Chroma集合失败: {}", e.getMessage());
            throw new RuntimeException("创建Chroma集合失败: " + e.getMessage());
        }
    }

    /**
     * 添加文档到集合（v2 API，显式提供 embeddings）
     */
    public void addDocuments(String collectionName, List<String> chunks, Long docId, String docName) {
        String collectionId = getOrCreateCollection(collectionName);

        try {
            String url = getBaseUrl() + "/api/v2/tenants/default_tenant/databases/default_database/collections/" + collectionId + "/add";

            ObjectNode body = objectMapper.createObjectNode();
            ArrayNode documents = body.putArray("documents");
            ArrayNode metadatas = body.putArray("metadatas");
            ArrayNode ids = body.putArray("ids");
            ArrayNode embeddings = body.putArray("embeddings");

            for (int i = 0; i < chunks.size(); i++) {
                documents.add(chunks.get(i));
                ObjectNode meta = objectMapper.createObjectNode();
                meta.put("doc_id", docId);
                meta.put("doc_name", docName);
                meta.put("chunk_index", i);
                metadatas.add(meta);
                ids.add(docId + "_" + i);

                List<Float> emb = generateEmbedding(chunks.get(i));
                ArrayNode embArray = embeddings.addArray();
                for (Float f : emb) {
                    embArray.add(f);
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            log.info("已向Chroma添加 {} 个文本块到集合 {}, status: {}", chunks.size(), collectionName, response.getStatusCode());
        } catch (Exception e) {
            log.error("添加文档到Chroma失败", e);
            throw new RuntimeException("向量存储失败: " + e.getMessage());
        }
    }

    /**
     * 查询相似文本（v2 API，显式提供 query embeddings）
     */
    public List<RetrievalResult> query(String collectionName, String queryText, int nResults) {
        String collectionId = getOrCreateCollection(collectionName);

        try {
            String url = getBaseUrl() + "/api/v2/tenants/default_tenant/databases/default_database/collections/" + collectionId + "/query";

            ObjectNode body = objectMapper.createObjectNode();
            ArrayNode queryEmbeddings = body.putArray("query_embeddings");
            List<Float> emb = generateEmbedding(queryText);
            ArrayNode embArray = queryEmbeddings.addArray();
            for (Float f : emb) {
                embArray.add(f);
            }
            body.put("n_results", nResults);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return parseQueryResponse(response.getBody());
        } catch (Exception e) {
            log.error("Chroma查询失败", e);
            return new ArrayList<>();
        }
    }

    private List<RetrievalResult> parseQueryResponse(String responseBody) {
        List<RetrievalResult> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode documents = root.path("documents");
            JsonNode metadatas = root.path("metadatas");
            JsonNode distances = root.path("distances");

            if (documents.isArray() && documents.size() > 0) {
                JsonNode docList = documents.get(0);
                JsonNode metaList = metadatas.get(0);
                JsonNode distList = distances.get(0);

                for (int i = 0; i < docList.size(); i++) {
                    RetrievalResult r = new RetrievalResult();
                    r.setContent(docList.get(i).asText());
                    r.setDocName(metaList.get(i).path("doc_name").asText(""));
                    r.setDistance(distList.get(i).asDouble());
                    results.add(r);
                }
            }
        } catch (Exception e) {
            log.warn("解析Chroma响应失败", e);
        }
        return results;
    }

    /**
     * 删除集合（v2 API）
     */
    public void deleteCollection(String collectionName) {
        try {
            // 先获取UUID
            String listUrl = getBaseUrl() + "/api/v2/tenants/default_tenant/databases/default_database/collections";
            ResponseEntity<String> listResponse = restTemplate.getForEntity(listUrl, String.class);
            JsonNode collections = objectMapper.readTree(listResponse.getBody());
            for (JsonNode c : collections) {
                if (collectionName.equals(c.path("name").asText())) {
                    String id = c.path("id").asText();
                    String url = getBaseUrl() + "/api/v2/tenants/default_tenant/databases/default_database/collections/" + id;
                    restTemplate.delete(url);
                    collectionIdCache.remove(collectionName);
                    log.info("已删除Chroma集合: {}", collectionName);
                    return;
                }
            }
        } catch (Exception e) {
            log.warn("删除Chroma集合失败: {}", e.getMessage());
        }
    }

    /**
     * 检索结果
     */
    public static class RetrievalResult {
        private String content;
        private String docName;
        private double distance;

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getDocName() { return docName; }
        public void setDocName(String docName) { this.docName = docName; }
        public double getDistance() { return distance; }
        public void setDistance(double distance) { this.distance = distance; }
    }
}
