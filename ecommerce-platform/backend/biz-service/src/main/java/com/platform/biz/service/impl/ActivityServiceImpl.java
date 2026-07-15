package com.platform.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.biz.dto.ActivityDTO;
import com.platform.biz.entity.Activity;
import com.platform.biz.entity.ActivityProduct;
import com.platform.biz.mapper.ActivityMapper;
import com.platform.biz.mapper.ActivityProductMapper;
import com.platform.biz.service.ActivityService;
import com.platform.biz.vo.ActivityDetailVO;
import com.platform.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 活动服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityProductMapper activityProductMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ActivityDTO dto) {
        log.info("创建活动: {}", dto.getName());
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
    public List<Activity> list() {
        log.info("查询活动列表");
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Activity::getCreateTime);
        return activityMapper.selectList(wrapper);
    }

    @Override
    public void publish(Long id) {
        log.info("发布活动: {}", id);
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
        Activity update = new Activity();
        update.setId(id);
        update.setStatus(1); // 已发布
        activityMapper.updateById(update);
    }

    @Override
    public void unpublish(Long id) {
        log.info("撤销发布活动: {}", id);
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
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
    public void delete(Long id) {
        log.info("删除活动: {}", id);
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException("活动不存在");
        }
        // 先删除关联的折扣商品
        LambdaQueryWrapper<ActivityProduct> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityProduct::getActivityId, id);
        activityProductMapper.delete(wrapper);
        // 再删除活动
        activityMapper.deleteById(id);
    }
}
