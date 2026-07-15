package com.platform.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.biz.entity.UserMessage;
import com.platform.biz.mapper.UserMessageMapper;
import com.platform.biz.service.MessageService;
import com.platform.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 消息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final UserMessageMapper userMessageMapper;

    @Override
    public List<UserMessage> list(Long userId, String type) {
        log.info("查询消息列表: userId={}, type={}", userId, type);
        LambdaQueryWrapper<UserMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMessage::getUserId, userId);

        if ("activity".equals(type)) {
            wrapper.eq(UserMessage::getType, 3);
        } else if ("buyer".equals(type)) {
            wrapper.in(UserMessage::getType, Arrays.asList(1, 4));
        } else if ("seller".equals(type)) {
            wrapper.in(UserMessage::getType, Arrays.asList(2, 4, 5));
        }
        // all 或其他不过滤 type

        wrapper.orderByDesc(UserMessage::getCreateTime);
        return userMessageMapper.selectList(wrapper);
    }

    @Override
    public void read(Long id) {
        log.info("标记消息已读: {}", id);
        UserMessage message = userMessageMapper.selectById(id);
        if (message == null) {
            throw new BizException("消息不存在");
        }
        UserMessage update = new UserMessage();
        update.setId(id);
        update.setIsRead(1);
        userMessageMapper.updateById(update);
    }
}
