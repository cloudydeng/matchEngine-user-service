package com.matching.user.service;

import com.matching.user.dto.DeviceInfo;
import com.matching.user.dto.LoginRequest;
import com.matching.user.dto.RegisterRequest;
import com.matching.user.entity.User;
import com.matching.user.entity.UserStatus;
import com.matching.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private int accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private int refreshTokenExpiration;

    // Redis Key 前缀
    private static final String USER_INFO_PREFIX = "user:info:";
    private static final String TOKEN_PREFIX = "user:token:";
    private static final String LOGIN_FAIL_PREFIX = "user:login:fail:";
    private static final String VERIFY_CODE_PREFIX = "user:verify:";
    private static final String RATE_LIMIT_PREFIX = "rate:limit:";

    /**
     * 用户注册
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 1. 验证码检查
        if (!verifyCode("email", request.getEmail(), request.getEmailVerificationCode())) {
            throw new RuntimeException("邮箱验证码错误");
        }

        // 2. 检查用户名/邮箱/手机号是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        if (request.getPhoneVerificationCode() != null &&
            userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }

        // 3. 密码加密
        String salt = UUID.randomUUID().toString().substring(0, 16);
        String passwordHash = passwordEncoder.hash(request.getPassword(), salt);

        // 4. 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhoneVerificationCode() != null ? request.getPhone() : null)
                .passwordHash(passwordHash)
                .salt(salt)
                .phoneVerified(request.getPhoneVerificationCode() != null)
                .emailVerified(true)  // 邮箱验证后自动设置
                .status(UserStatus.ACTIVE)
                .referralCode(request.getReferralCode())
                .build();

        userRepository.save(user);

        // 5. 保存用户信息到 Redis
        String key = USER_INFO_PREFIX + user.getUserId();
        redisTemplate.opsForHash().put(key, "username", user.getUsername());
        redisTemplate.opsForHash().put(key, "email", user.getEmail());
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        log.info("User registered: userId={}, username={}", user.getUserId(), user.getUsername());
    }

    /**
     * 用户登录
     */
    public User login(LoginRequest request, DeviceInfo deviceInfo) {
        // 1. 检查登录限流
        String failKey = LOGIN_FAIL_PREFIX + request.getUsername();
        Long failCount = redisTemplate.opsForValue().get(failKey);
        if (failCount != null && failCount >= 5) {
            // 清除计数
            redisTemplate.delete(failKey);
        }

        // 2. 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 3. 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("账户已被禁用或锁定");
        }

        // 4. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // 登录失败，增加计数
            redisTemplate.opsForValue().increment(failKey);
            redisTemplate.expire(failKey, 15, TimeUnit.MINUTES);
            throw new RuntimeException("用户名或密码错误");
        }

        // 5. 登录成功，清除失败计数
        redisTemplate.delete(failKey);

        // 6. 记录设备信息
        recordDevice(user.getUserId(), deviceInfo);

        // 7. 更新用户信息到 Redis
        String key = USER_INFO_PREFIX + user.getUserId();
        redisTemplate.opsForHash().put(key, "username", user.getUsername());
        redisTemplate.opsForHash().put(key, "email", user.getEmail());
        redisTemplate.opsForHash().put(key, "userId", user.getUserId().toString());
        redisTemplate.opsForHash().put(key, "lastLoginTime", System.currentTimeMillis());
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        log.info("User login success: userId={}, username={}",
                user.getUserId(), user.getUsername());

        return user;
    }

    /**
     * 生成 Access Token
     */
    public String generateAccessToken(Long userId, String username) {
        // 这里应该使用 JWT，简化处理
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = TOKEN_PREFIX + token;

        redisTemplate.opsForValue().set(key, userId.toString());
        redisTemplate.expire(key, accessTokenExpiration, TimeUnit.SECONDS);

        return token;
    }

    /**
     * 生成 Refresh Token
     */
    public String generateRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString().replace("-", "") + "_refresh";
        String key = TOKEN_PREFIX + token;

        redisTemplate.opsForValue().set(key, userId.toString());
        redisTemplate.expire(key, refreshTokenExpiration, TimeUnit.SECONDS);

        return token;
    }

    /**
     * 验证 Token
     */
    public Long verifyToken(String token) {
        String key = TOKEN_PREFIX + token;
        String userIdStr = redisTemplate.opsForValue().get(key);

        if (userIdStr == null) {
            return null;
        }

        return Long.parseLong(userIdStr);
    }

    /**
     * 发送验证码
     */
    public void sendVerificationCode(String type, String destination) {
        // 生成 6 位数字验证码
        String code = String.format("%06d", (int)(Math.random() * 900000) + 100000);

        // 保存到 Redis（5 分钟过期）
        String key = VERIFY_CODE_PREFIX + type + ":" + destination;
        redisTemplate.opsForValue().set(key, code);
        redisTemplate.expire(key, 5, TimeUnit.MINUTES);

        // 限制发送频率
        String rateKey = RATE_LIMIT_PREFIX + type + ":" + destination;
        Long count = redisTemplate.opsForValue().get(rateKey);
        if (count != null && count >= 5) {
            throw new RuntimeException("发送频率过快，请稍后再试");
        }

        redisTemplate.opsForValue().increment(rateKey);
        redisTemplate.expire(rateKey, 1, TimeUnit.HOURS);

        log.info("Verification code sent: type={}, destination={}", type, destination);

        // 实际应该发送到短信/邮件服务
        // 这里简化处理，直接返回验证码用于测试
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String type, String destination, String code) {
        String key = VERIFY_CODE_PREFIX + type + ":" + destination;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            return false;
        }

        boolean valid = savedCode.equals(code);
        if (valid) {
            redisTemplate.delete(key);
        }

        return valid;
    }

    /**
     * 记录设备信息
     */
    private void recordDevice(Long userId, DeviceInfo deviceInfo) {
        // 简化处理，实际应该保存到数据库
        String key = "device:" + deviceInfo.getDeviceId();
        redisTemplate.opsForHash().put(key, "userId", userId.toString());
        redisTemplate.opsForHash().put(key, "lastActive", System.currentTimeMillis());
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
    }
}
