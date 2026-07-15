package com.platform.biz.config;

import com.platform.biz.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/biz/**")
                .excludePathPatterns(
                        "/biz/user/login",
                        "/biz/user/register",
                        "/biz/product/onSaleList",
                        "/biz/product/detail/**",
                        "/biz/vip/config"
                );
    }
}