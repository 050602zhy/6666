package com.platform.biz.service;

import com.platform.biz.dto.ActivityDTO;
import com.platform.biz.entity.Activity;
import com.platform.biz.vo.ActivityDetailVO;

import java.util.List;

/**
 * 活动服务接口
 */
public interface ActivityService {

    /**
     * 创建活动
     *
     * @param dto 活动信息
     */
    void create(ActivityDTO dto);

    /**
     * 查询活动列表
     *
     * @return 活动列表
     */
    List<Activity> list();

    /**
     * 发布活动
     *
     * @param id 活动ID
     */
    void publish(Long id);

    /**
     * 撤销发布活动
     *
     * @param id 活动ID
     */
    void unpublish(Long id);

    /**
     * 获取活动详情（含折扣商品）
     *
     * @param id 活动ID
     * @return 活动详情
     */
    ActivityDetailVO getDetail(Long id);

    /**
     * 删除活动
     *
     * @param id 活动ID
     */
    void delete(Long id);
}
