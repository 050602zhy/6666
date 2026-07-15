package com.platform.biz.interceptor;

import com.platform.biz.entity.User;
import com.platform.biz.mapper.UserMapper;
import com.platform.common.exception.BizException;
import com.platform.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录校验拦截器
 * 从请求头 X-User-Id 获取用户ID，校验用户是否存在
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            writeUnauthorized(response, "未登录，请先登录");
            return false;
        }

        try {
            Long userId = Long.valueOf(userIdStr);
            User user = userMapper.selectById(userId);
            if (user == null) {
                writeUnauthorized(response, "用户不存在");
                return false;
            }
            // 将用户ID和角色存入请求属性，供后续 Controller 使用
            request.setAttribute("currentUserId", userId);
            request.setAttribute("currentUserRole", user.getRole());
            return true;
        } catch (NumberFormatException e) {
            writeUnauthorized(response, "用户ID格式错误");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}