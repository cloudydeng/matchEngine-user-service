package com.matching.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 测试环境配置 - 排除需要数据库和邮件服务的 Bean
 */
@Configuration
@Profile("test")
public class TestProfileConfig {

    /**
     * 测试模式的 AuthService - 不依赖数据库
     */
    @Bean
    @ConditionalOnProperty(name = "test.auth.mock", havingValue = "true", matchIfMissing = true)
    public com.matching.user.service.AuthService testAuthService() {
        // 返回一个模拟的 AuthService，不依赖数据库和 Redis
        return new com.matching.user.service.AuthService() {
            @Override
            public void register(com.matching.user.dto.RegisterRequest request) {
                // 模拟注册成功
            }

            @Override
            public com.matching.user.entity.User login(com.matching.user.dto.LoginRequest request, com.matching.user.dto.DeviceInfo deviceInfo) {
                // 模拟登录成功
                com.matching.user.entity.User user = new com.matching.user.entity.User();
                user.setUserId(1L);
                user.setUsername(request.getUsername());
                return user;
            }

            @Override
            public String generateAccessToken(Long userId, String username) {
                return "test-token";
            }

            @Override
            public String generateRefreshToken(Long userId) {
                return "test-refresh-token";
            }

            @Override
            public Long verifyToken(String token) {
                if ("test-token".equals(token)) {
                    return 1L;
                }
                return null;
            }

            @Override
            public void sendVerificationCode(String type, String destination) {
                // 模拟发送成功
            }

            @Override
            public boolean verifyCode(String type, String destination, String code) {
                // 模拟验证码验证通过
                return true;
            }
        };
    }

    /**
     * 测试模式的 TokenService - 不依赖 Redis
     */
    @Bean
    @ConditionalOnProperty(name = "test.auth.mock", havingValue = "true", matchIfMissing = true)
    public com.matching.user.service.TokenService testTokenService() {
        return new com.matching.user.service.TokenService() {
            @Override
            public Long verifyToken(String token) {
                if ("test-token".equals(token)) {
                    return 1L;
                }
                return null;
            }

            @Override
            public void revokeToken(String token) {
                // 测试环境无实际操作
            }
        };
    }
}
