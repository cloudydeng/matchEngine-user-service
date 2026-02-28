package com.matching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;

/**
 * 测试环境专用启动类 - 不加载用户服务组件
 */
@SpringBootApplication
@Profile("test")
public class TestUserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestUserServiceApplication.class, args);
    }
}
