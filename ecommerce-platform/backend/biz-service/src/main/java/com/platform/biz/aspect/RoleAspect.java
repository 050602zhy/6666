package com.platform.biz.aspect;

import com.platform.biz.annotation.RequireRole;
import com.platform.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * 角色鉴权切面
 * 拦截标注了 @RequireRole 的方法，校验当前用户角色
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RoleAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BizException("无法获取请求上下文");
        }
        HttpServletRequest request = attrs.getRequest();
        String currentRole = (String) request.getAttribute("currentUserRole");

        if (currentRole == null) {
            throw new BizException("未登录，请先登录");
        }

        String[] allowedRoles = requireRole.value();
        boolean hasPermission = Arrays.asList(allowedRoles).contains(currentRole);
        if (!hasPermission) {
            throw new BizException("无权执行此操作，需要角色：" + String.join(" 或 ", allowedRoles));
        }
    }
}