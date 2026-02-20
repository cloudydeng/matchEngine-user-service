package com.matching.user.controller;

import com.matching.user.dto.CommonResponse;
import com.matching.user.dto.DeviceInfo;
import com.matching.user.dto.LoginRequest;
import com.matching.user.dto.RegisterRequest;
import com.matching.user.entity.User;
import com.matching.user.service.AuthService;
import com.matching.user.service.TokenService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public CommonResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);

            // 简化：这里没有实际创建用户，返回模拟数据
            RegisterResponse response = RegisterResponse.builder()
                    .userId(123L)
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .build();

            return CommonResponse.success(response);

        } catch (RuntimeException e) {
            log.error("Register failed: {}", e.getMessage());
            return CommonResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Register error", e);
            return CommonResponse.error("注册失败");
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public CommonResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 提取设备信息（实际应该从请求头获取）
            DeviceInfo deviceInfo = request.getDeviceInfo();
            if (deviceInfo == null) {
                deviceInfo = DeviceInfo.builder()
                        .deviceId(java.util.UUID.randomUUID().toString())
                        .deviceType("WEB")
                        .build();
            }

            User user = authService.login(request, deviceInfo);

            String accessToken = tokenService.generateAccessToken(user.getUserId(), user.getUsername());
            String refreshToken = tokenService.generateRefreshToken(user.getUserId());

            LoginResponse response = LoginResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(7200)  // 2 小时
                    .build();

            return CommonResponse.success(response);

        } catch (RuntimeException e) {
            log.error("Login failed: {}", e.getMessage());
            return CommonResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Login error", e);
            return CommonResponse.error("登录失败");
        }
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public CommonResponse<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            Long userId = tokenService.verifyToken(request.getRefreshToken());

            if (userId == null) {
                return CommonResponse.error("Refresh Token 无效");
            }

            String accessToken = tokenService.generateAccessToken(userId, null);
            TokenResponse response = TokenResponse.builder()
                    .accessToken(accessToken)
                    .expiresIn(7200)
                    .build();

            return CommonResponse.success(response);

        } catch (Exception e) {
            log.error("Refresh token failed", e);
            return CommonResponse.error("刷新 Token 失败");
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    public CommonResponse<UserInfoResponse> getUserInfo(
            @RequestHeader("Authorization") String authorization) {
        try {
            // 提取 Token
            String token = authorization.replace("Bearer ", "");
            Long userId = tokenService.verifyToken(token);

            if (userId == null) {
                return CommonResponse.error("Token 无效");
            }

            // 查询用户信息
            User user = authService.getUserById(userId);

            UserInfoResponse response = UserInfoResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .status(user.getStatus().name())
                    .createdAt(user.getCreatedAt())
                    .build();

            return CommonResponse.success(response);

        } catch (Exception e) {
            log.error("Get user info failed", e);
            return CommonResponse.error("获取用户信息失败");
        }
    }

    /**
     * 发送验证码
     */
    @PostMapping("/verify/send")
    public CommonResponse<String> sendVerificationCode(@RequestBody VerificationRequest request) {
        try {
            authService.sendVerificationCode(request.getType(), request.getDestination());
            return CommonResponse.success("验证码已发送");

        } catch (RuntimeException e) {
            log.error("Send verification code failed: {}", e.getMessage());
            return CommonResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Send verification code error", e);
            return CommonResponse.error("发送验证码失败");
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public CommonResponse<String> health() {
        return CommonResponse.success("OK");
    }

    // DTO 定义
    @Data
    @Builder
    static class RegisterResponse {
        private Long userId;
        private String username;
        private String email;
    }

    @Data
    @Builder
    static class LoginResponse {
        private Long userId;
        private String username;
        private String email;
        private String accessToken;
        private String refreshToken;
        private Integer expiresIn;
    }

    @Data
    @Builder
    static class TokenResponse {
        private String accessToken;
        private Integer expiresIn;
    }

    @Data
    @Builder
    static class UserInfoResponse {
        private Long userId;
        private String username;
        private String email;
        private String phone;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    static class RefreshTokenRequest {
        private String refreshToken;
    }

    @Data
    static class VerificationRequest {
        private String type;      // EMAIL, PHONE
        private String destination; // 邮箱或手机号
    }
}
