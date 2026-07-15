package com.platform.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.biz.entity.UserMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户消息 Mapper 接口
 */
@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {
}
