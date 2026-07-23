package com.platform.biz.service;

import com.platform.biz.dto.ActivityDTO;
import com.platform.biz.entity.Activity;
import com.platform.biz.entity.Product;
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
     * 查询指定卖家创建的活动列表
     *
     * @param creatorId 创建者ID
     * @return 活动列表
     */
    List<Activity> list(Long creatorId);

    /**
     * 发布活动
     *
     * @param id 活动ID
     * @param creatorId 操作者ID（用于权限校验）
     */
    void publish(Long id, Long creatorId);

    /**
     * 撤销发布活动
     *
     * @param id 活动ID
     * @param creatorId 操作者ID（用于权限校验）
     */
    void unpublish(Long id, Long creatorId);

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
     * @param creatorId 操作者ID（用于权限校验）
     */
    void delete(Long id, Long creatorId);

    /**
     * 获取当前正在进行的活动中的所有商品（已发布且在有效期内）
     *
     * @return 活动中的商品列表（含折扣信息）
     */
    List<Product> listActiveActivityProducts();
}
