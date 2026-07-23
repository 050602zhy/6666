package com.platform.ai.service.impl;

import com.platform.ai.service.RecommendService;
import com.platform.ai.vo.RecommendProductVO;
import com.platform.common.result.Result;
import com.platform.feign.api.BizServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能推荐服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final BizServiceClient bizServiceClient;

    @Override
    public List<RecommendProductVO> recommend(Long userId) {
        log.info("为用户 [{}] 生成推荐", userId);

        // 1. 获取用户信息
        Map<String, Object> user = getUser(userId);
        String preference = null;
        boolean personalizedOn = false;
        if (user != null) {
            Object prefObj = user.get("preference");
            preference = prefObj != null ? prefObj.toString().trim() : null;
            Object personalizedObj = user.get("personalizedRecommend");
            if (personalizedObj != null) {
                personalizedOn = Integer.parseInt(personalizedObj.toString()) == 1;
            }
        }

        // 2. 获取所有上架商品
        List<Map<String, Object>> allProducts = getOnSaleProducts();
        if (allProducts == null || allProducts.isEmpty()) {
            log.warn("没有上架商品可推荐");
            return Collections.emptyList();
        }

        // 3. 获取活动商品
        List<Map<String, Object>> activityProducts = getActiveActivityProducts();
        Set<Long> activityProductIds = new HashSet<>();
        Map<Long, Map<String, Object>> activityProductMap = new HashMap<>();
        if (activityProducts != null) {
            for (Map<String, Object> ap : activityProducts) {
                Long id = getLong(ap.get("id"));
                if (id != null) {
                    activityProductIds.add(id);
                    activityProductMap.put(id, ap);
                }
            }
        }

        // 4. 判断是否有偏好且个性化推荐开启
        boolean hasPreference = preference != null && !preference.isEmpty() && personalizedOn;

        // 5. 获取偏好分类ID（如果有偏好）
        Long preferenceCategoryId = null;
        if (hasPreference) {
            preferenceCategoryId = findCategoryIdByName(preference);
            if (preferenceCategoryId == null) {
                log.warn("未找到偏好分类 [{}]，按无偏好处理", preference);
                hasPreference = false;
            }
        }

        List<RecommendProductVO> result;
        if (!hasPreference) {
            // 无偏好场景
            result = recommendWithoutPreference(allProducts, activityProductIds, activityProductMap);
        } else {
            // 有偏好场景
            result = recommendWithPreference(allProducts, activityProductIds, activityProductMap, preferenceCategoryId);
        }

        log.info("为用户 [{}] 生成了 [{}] 条推荐", userId, result.size());
        return result;
    }

    /**
     * 无偏好（或个性化推荐关闭）的推荐逻辑
     * 先推荐活动商品（按评分降序、ID升序），再推荐非活动商品（按评分降序、ID升序）
     */
    private List<RecommendProductVO> recommendWithoutPreference(
            List<Map<String, Object>> allProducts,
            Set<Long> activityProductIds,
            Map<Long, Map<String, Object>> activityProductMap) {

        // 分离活动商品和非活动商品
        List<ProductInfo> activityList = new ArrayList<>();
        List<ProductInfo> nonActivityList = new ArrayList<>();

        for (Map<String, Object> product : allProducts) {
            Long id = getLong(product.get("id"));
            if (id == null) continue;

            ProductInfo info = buildProductInfo(product, activityProductIds.contains(id), activityProductMap.get(id));
            if (activityProductIds.contains(id)) {
                activityList.add(info);
            } else {
                nonActivityList.add(info);
            }
        }

        // 排序：评分降序，ID升序
        sortProducts(activityList);
        sortProducts(nonActivityList);

        // 计算匹配度和推荐理由
        List<RecommendProductVO> result = new ArrayList<>();
        for (ProductInfo info : activityList) {
            result.add(toRecommendVO(info, 50, "限时折扣活动中"));
        }
        for (ProductInfo info : nonActivityList) {
            result.add(toRecommendVO(info, 30, "好评如潮"));
        }

        return result;
    }

    /**
     * 有偏好（且个性化推荐开启）的推荐逻辑
     * Step 1: 活动中匹配偏好的商品（全部）
     * Step 2: 活动中不匹配偏好的商品（最多10个）
     * Step 3: 非活动中匹配偏好的商品（全部）
     */
    private List<RecommendProductVO> recommendWithPreference(
            List<Map<String, Object>> allProducts,
            Set<Long> activityProductIds,
            Map<Long, Map<String, Object>> activityProductMap,
            Long preferenceCategoryId) {

        // 分类：活动中匹配偏好 / 活动中不匹配偏好 / 非活动中匹配偏好
        List<ProductInfo> activityMatchList = new ArrayList<>();
        List<ProductInfo> activityNoMatchList = new ArrayList<>();
        List<ProductInfo> nonActivityMatchList = new ArrayList<>();

        for (Map<String, Object> product : allProducts) {
            Long id = getLong(product.get("id"));
            if (id == null) continue;

            boolean inActivity = activityProductIds.contains(id);
            ProductInfo info = buildProductInfo(product, inActivity, activityProductMap.get(id));
            boolean matchesPreference = preferenceCategoryId.equals(info.categoryId);

            if (inActivity && matchesPreference) {
                activityMatchList.add(info);
            } else if (inActivity) {
                activityNoMatchList.add(info);
            } else if (matchesPreference) {
                nonActivityMatchList.add(info);
            }
            // 非活动非偏好：不推荐
        }

        // 排序：评分降序，ID升序
        sortProducts(activityMatchList);
        sortProducts(activityNoMatchList);
        sortProducts(nonActivityMatchList);

        // 组装结果
        List<RecommendProductVO> result = new ArrayList<>();

        // Step 1: 活动中匹配偏好
        for (ProductInfo info : activityMatchList) {
            result.add(toRecommendVO(info, 100, "符合您的购物偏好，限时折扣活动中"));
        }

        // Step 2: 活动中不匹配偏好，最多10个
        int count = 0;
        for (ProductInfo info : activityNoMatchList) {
            if (count >= 10) break;
            result.add(toRecommendVO(info, 50, "限时折扣活动中"));
            count++;
        }

        // Step 3: 非活动中匹配偏好
        for (ProductInfo info : nonActivityMatchList) {
            result.add(toRecommendVO(info, 70, "符合您的购物偏好"));
        }

        return result;
    }

    /**
     * 从Map构建商品信息对象
     */
    private ProductInfo buildProductInfo(Map<String, Object> product, boolean inActivity, Map<String, Object> activityProduct) {
        ProductInfo info = new ProductInfo();
        info.id = getLong(product.get("id"));
        info.name = getStr(product.get("name"));
        info.image = getStr(product.get("image"));
        info.price = getBigDecimal(product.get("price"));
        info.rating = getBigDecimal(product.get("rating"));
        info.categoryId = getLong(product.get("categoryId"));
        info.inActivity = inActivity;

        if (inActivity && activityProduct != null) {
            info.discountPrice = getBigDecimal(activityProduct.get("discountPrice"));
            if (info.discountPrice == null) {
                // 尝试从product本身取（onSaleList返回的product可能已经填了折扣）
                info.discountPrice = getBigDecimal(product.get("discountPrice"));
            }
        }

        return info;
    }

    /**
     * 排序：评分降序，ID升序
     */
    private void sortProducts(List<ProductInfo> list) {
        list.sort((a, b) -> {
            BigDecimal ratingA = a.rating != null ? a.rating : BigDecimal.ZERO;
            BigDecimal ratingB = b.rating != null ? b.rating : BigDecimal.ZERO;
            int cmp = ratingB.compareTo(ratingA); // 评分降序
            if (cmp != 0) return cmp;
            Long idA = a.id != null ? a.id : 0L;
            Long idB = b.id != null ? b.id : 0L;
            return idA.compareTo(idB); // ID升序
        });
    }

    /**
     * 转换为推荐VO
     */
    private RecommendProductVO toRecommendVO(ProductInfo info, int matchScore, String reason) {
        RecommendProductVO vo = new RecommendProductVO();
        vo.setId(info.id);
        vo.setName(info.name);
        vo.setImage(info.image);
        vo.setPrice(info.price);
        vo.setDiscountPrice(info.discountPrice);
        // 评分截断小数（显示用）
        if (info.rating != null) {
            vo.setRating(info.rating.intValue());
        } else {
            vo.setRating(0);
        }
        vo.setCategoryId(info.categoryId);
        vo.setMatchScore(matchScore);
        vo.setReason(reason);
        return vo;
    }

    /**
     * 根据分类名称查找分类ID
     */
    private Long findCategoryIdByName(String categoryName) {
        try {
            Result<List<Map<String, Object>>> result = bizServiceClient.listCategories();
            if (result != null && result.getCode() != null && result.getCode() == 200 && result.getData() != null) {
                for (Map<String, Object> cat : result.getData()) {
                    String name = getStr(cat.get("name"));
                    if (categoryName.equals(name)) {
                        return getLong(cat.get("id"));
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
        }
        return null;
    }

    /**
     * 获取用户信息
     */
    private Map<String, Object> getUser(Long userId) {
        try {
            Result<Map<String, Object>> result = bizServiceClient.getUserById(userId);
            if (result != null && result.getCode() != null && result.getCode() == 200) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("获取用户信息失败: userId={}", userId, e);
        }
        return null;
    }

    /**
     * 获取所有上架商品
     */
    private List<Map<String, Object>> getOnSaleProducts() {
        try {
            Result<List<Map<String, Object>>> result = bizServiceClient.listOnSaleProducts();
            if (result != null && result.getCode() != null && result.getCode() == 200) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("获取上架商品列表失败", e);
        }
        return Collections.emptyList();
    }

    /**
     * 获取活动商品
     */
    private List<Map<String, Object>> getActiveActivityProducts() {
        try {
            Result<List<Map<String, Object>>> result = bizServiceClient.listActiveActivityProducts();
            if (result != null && result.getCode() != null && result.getCode() == 200) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("获取活动商品列表失败", e);
        }
        return Collections.emptyList();
    }

    // === 工具方法 ===

    private Long getLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String getStr(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private BigDecimal getBigDecimal(Object obj) {
        if (obj == null) return null;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        if (obj instanceof Number) return new BigDecimal(obj.toString());
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 内部商品信息类
     */
    private static class ProductInfo {
        Long id;
        String name;
        String image;
        BigDecimal price;
        BigDecimal discountPrice;
        BigDecimal rating;
        Long categoryId;
        boolean inActivity;
    }
}
