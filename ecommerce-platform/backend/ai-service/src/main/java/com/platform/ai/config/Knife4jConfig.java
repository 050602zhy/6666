package com.platform.ai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j (Swagger增强版) 配置类
 * <p>
 * 为 AI 智能服务模块生成网页版 API 接口文档
 * 访问地址: http://localhost:8081/doc.html
 * <p>
 * 注意: 本项目使用 Spring Boot 3.2.0，对应 Knife4j 4.x 版本（基于 SpringDoc OpenAPI）
 *
 * @author platform
 */
@Configuration
public class Knife4jConfig {

    /**
     * 配置 OpenAPI 文档基本信息
     * Knife4j 4.x 基于 SpringDoc，通过 OpenAPI Bean 配置文档元数据
     *
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI aiServiceOpenAPI() {
        return new OpenAPI()
                // 文档基本信息
                .info(new Info()
                        // 标题
                        .title("AI 智能服务 - API 接口文档")
                        // 版本号
                        .version("v1.0.0")
                        // 描述
                        .description("智能电商运营平台 - AI 智能服务模块接口文档<br/>"
                                + "包含智能推荐、智能客服、数据分析等接口")
                        // 联系人信息
                        .contact(new Contact()
                                .name("平台开发团队")
                                .email("dev@platform.com"))
                        // 许可证信息
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
