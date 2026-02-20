package com.matching.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token 服务
 */
@Slf4j
@Service
public class TokenService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String USER_INFO_PREFIX = "user:info:";
    private static final String TOKEN_PREFIX = "user:token:";
    private static final String TOKEN_BLACKLIST = "token:blacklist:";

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(Long userId, String username) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = TOKEN_PREFIX + "access:" + token;

        redisTemplate.opsForValue().set(key, userId.toString());
        redisTemplate.expire(key, 2, TimeUnit.HOURS);

        log.debug("Access token generated for userId={}", userId);
        return token;
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "") + "_refresh";
        String key = TOKEN_PREFIX + "refresh:" + token;

        redisTemplate.opsForValue().set(key, userId.toString());
        redisTemplate.expire(key, 7, TimeUnit.DAYS);

        log.debug("Refresh token generated for userId={}", userId);
        return token;
    }

    /**
     * 验证 Token
     */
    public Long verifyToken(String token) {
        // 检查 Token 黑名单
        String blacklistKey = TOKEN_BLACKLIST + token;
        if (Boolean.TRUE.equals(redisTemplate.opsForValue().get(blacklistKey))) {
            log.warn("Token is in blacklist: {}", token);
            return null;
        }

        // 检查 Access Token
        String accessKey = TOKEN_PREFIX + "access:" + token;
        String userIdStr = (String) redisTemplate.opsForValue().get(accessKey);

        if (userIdStr != null) {
            return Long.parseLong(userIdStr);
        }

        // 检查 Refresh Token
        String refreshKey = TOKEN_PREFIX + "refresh:" + token;
        userIdStr = (String) redisTemplate.opsForValue().get(refreshKey);

        if (userIdStr != null) {
            return Long.parseLong(userIdStr);
        }

        log.warn("Token not found: {}", token);
        return null;
    }

    /**
     * 撤销 Token（用户登出时调用）
     */
    public void revokeToken(String token) {
        // 添加到黑名单
        String blacklistKey = TOKEN_BLACKLIST + token;
        redisTemplate.opsForValue().set(blacklistKey, Boolean.TRUE);
        redisTemplate.expire(blacklistKey, 2, TimeUnit.HOURS);

        // 删除 Token
        String accessKey = TOKEN_PREFIX + "access:" + token;
        String refreshKey = TOKEN_PREFIX + "refresh:" + token;
        redisTemplate.delete(accessKey);
        redisTemplate.delete(refreshKey);

        log.info("Token revoked: {}", token);
    }

    /**
     * 撤销用户所有 Token（禁用用户时调用）
     */
    public void revokeAllTokens(Long userId) {
        // 查询用户信息获取所有 Token
        String userInfoKey = USER_INFO_PREFIX + userId;
        String username = (String) redisTemplate.opsForHash().get(userInfoKey, "username");

        if (username != null) {
            // 删除用户信息缓存
            redisTemplate.delete(userInfoKey);

            log.info("All tokens revoked for userId={}", userId);
        }
    }
}
