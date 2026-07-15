package com.platform.biz.service;

import com.platform.biz.entity.UserMessage;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 查询消息列表
     *
     * @param userId 用户ID
     * @param type   消息类型: all-全部 buyer-买家相关 seller-卖家相关 activity-活动
     * @return 消息列表
     */
    List<UserMessage> list(Long userId, String type);

    /**
     * 标记消息已读
     *
     * @param id 消息ID
     */
    void read(Long id);
}
