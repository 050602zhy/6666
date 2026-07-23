package com.platform.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.biz.dto.ActivityDTO;
import com.platform.biz.entity.Activity;
import com.platform.biz.entity.ActivityProduct;
import com.platform.biz.entity.Product;
import com.platform.biz.mapper.ActivityMapper;
import com.platform.biz.mapper.ActivityProductMapper;
import com.platform.biz.mapper.ProductMapper;
import com.platform.biz.service.ActivityService;
import com.platform.biz.vo.ActivityDetailVO;
import com.platform.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityProductMapper activityProductMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ActivityDTO dto) {
        log.info("创建活动: {}", dto.getName());
        // 验证折扣商品的折扣率（必须在0.1~0.9之间，即1折到9折）
        if (dto.getActivityProducts() != null && !dto.getActivityProducts().isEmpty()) {
            for (ActivityProduct ap : dto.getActivityProducts()) {
                if (ap.getDiscount() == null) {
                    throw new BizException("折扣商品的折扣率不能为空");
                }
                if (ap.getDiscount().compareTo(new BigDecimal("0.1")) < 0
                        || ap.getDiscount().compareTo(new BigDecimal("0.9")) > 0) {
                    throw new BizException("折扣率必须在0.1到0.9之间（1折到9折）");
                }
            }
        }
        Activity activity = new Activity();
        activity.setName(dto.getName());
        activity.setDescription(dto.getDescription());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setStatus(0); // 草稿
        activity.setCreatorId(dto.getCreatorId());
        activityMapper.insert(activity);

        if (dto.getActivityProducts() != null && !dto.getActivityProducts().isEmpty()) {
            for (ActivityProduct ap : dto.getActivityProducts()) {
                ap.setActivityId(activity.getId());
                activityProductMapper.insert(ap);
            }
        }
    }

    @Override
    public List<Activity> list(Long creatorId) {
        log.info("查询活动列表: creatorId={}", creatorId);
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getCreatorId, creatorId);
        wrapper.orderByDesc(Activity::getCreateTime);
        return activityMapper.selectList(wrapper);
    }

    @Override
    public void publish(Long id, Long creatorId) {
        log.info("发布活动: {}", id);
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
        checkOwner(activity, creatorId);

        // 检查该活动关联的商品是否已被其他已发布活动占用
        LambdaQueryWrapper<ActivityProduct> apWrapper = new LambdaQueryWrapper<>();
        apWrapper.eq(ActivityProduct::getActivityId, id);
        List<ActivityProduct> products = activityProductMapper.selectList(apWrapper);

        for (ActivityProduct ap : products) {
            LambdaQueryWrapper<ActivityProduct> conflictWrapper = new LambdaQueryWrapper<>();
            conflictWrapper.eq(ActivityProduct::getProductId, ap.getProductId());
            List<ActivityProduct> conflictAps = activityProductMapper.selectList(conflictWrapper);

            for (ActivityProduct cap : conflictAps) {
                if (cap.getActivityId().equals(id)) {
                    continue; // 跳过当前活动自身
                }
                Activity otherActivity = activityMapper.selectById(cap.getActivityId());
                if (otherActivity != null && otherActivity.getStatus() != null && otherActivity.getStatus() == 1) {
                    Product product = productMapper.selectById(ap.getProductId());
                    String productName = product != null ? product.getName() : String.valueOf(ap.getProductId());
                    throw new BizException("商品「" + productName + "」已被其他已发布活动关联，请先撤销原活动再发布");
                }
            }
        }

        Activity update = new Activity();
        update.setId(id);
        update.setStatus(1); // 已发布
        activityMapper.updateById(update);
    }

    @Override
    public void unpublish(Long id, Long creatorId) {
        log.info("撤销发布活动: {}", id);
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
        checkOwner(activity, creatorId);
        Activity update = new Activity();
        update.setId(id);
        update.setStatus(0); // 草稿
        activityMapper.updateById(update);
    }

    @Override
    public ActivityDetailVO getDetail(Long id) {
        log.info("查询活动详情: {}", id);
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
        LambdaQueryWrapper<ActivityProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityProduct::getActivityId, id);
        List<ActivityProduct> products = activityProductMapper.selectList(wrapper);

        ActivityDetailVO vo = new ActivityDetailVO();
        vo.setActivity(activity);
        vo.setActivityProducts(products);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long creatorId) {
        log.info("删除活动: {}", id);
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
        checkOwner(activity, creatorId);
        // 先删除关联的折扣商品
        LambdaQueryWrapper<ActivityProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityProduct::getActivityId, id);
        activityProductMapper.delete(wrapper);
        // 再删除活动
        activityMapper.deleteById(id);
    }

    @Override
    public List<Product> listActiveActivityProducts() {
        log.info("查询当前进行中活动的所有商品");
        LocalDateTime now = LocalDateTime.now();

        // 1. 查询所有已发布的活动
        LambdaQueryWrapper<Activity> activityWrapper = new LambdaQueryWrapper<>();
        activityWrapper.eq(Activity::getStatus, 1); // 已发布
        List<Activity> publishedActivities = activityMapper.selectList(activityWrapper);

        // 2. 筛选出时间有效的活动
        List<Long> activeActivityIds = new ArrayList<>();
        for (Activity activity : publishedActivities) {
            boolean timeValid = true;
            if (activity.getStartTime() != null && activity.getStartTime().isAfter(now)) {
                timeValid = false;
            }
            if (activity.getEndTime() != null && activity.getEndTime().isBefore(now)) {
                timeValid = false;
            }
            if (timeValid) {
                activeActivityIds.add(activity.getId());
            }
        }

        if (activeActivityIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 查询这些活动关联的所有商品（去重，取最佳折扣）
        LambdaQueryWrapper<ActivityProduct> apWrapper = new LambdaQueryWrapper<>();
        apWrapper.in(ActivityProduct::getActivityId, activeActivityIds);
        List<ActivityProduct> allActivityProducts = activityProductMapper.selectList(apWrapper);

        // 按 productId 分组，取最佳折扣（折后价最低）
        Set<Long> productIds = new HashSet<>();
        for (ActivityProduct ap : allActivityProducts) {
            productIds.add(ap.getProductId());
        }

        if (productIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Product> products = productMapper.selectBatchIds(productIds);

        // 过滤掉已下架的商品
        products = products.stream()
                .filter(p -> p.getOnSale() != null && p.getOnSale() == 1)
                .collect(Collectors.toList());

        // 为每个商品填充最佳折扣信息
        for (Product product : products) {
            ActivityProduct bestAp = null;
            for (ActivityProduct ap : allActivityProducts) {
                if (ap.getProductId().equals(product.getId())) {
                    if (bestAp == null || ap.getDiscountPrice().compareTo(bestAp.getDiscountPrice()) < 0) {
                        bestAp = ap;
                    }
                }
            }
            if (bestAp != null) {
                product.setDiscount(bestAp.getDiscount());
                product.setDiscountPrice(bestAp.getDiscountPrice());
                product.setOriginalPrice(bestAp.getOriginalPrice());
            }
        }

        return products;
    }

    /**
     * 校验活动是否属于指定创建者
     */
    private void checkOwner(Activity activity, Long creatorId) {
        if (!creatorId.equals(activity.getCreatorId())) {
            throw new BizException("无权操作该活动");
        }
    }
}
