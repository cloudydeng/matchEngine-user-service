package com.matching.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 测试环境配置 - 排除需要数据库的 Bean
 */
@Configuration
@Profile("test")
public class TestProfileConfig {
    // Test profile 用于在没有数据库的情况下验证代码编译
    // AuthService 和 TokenService 不会被加载
}
