package com.matching.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 登录请求
 */
@Data
@Builder
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String captcha;  // 验证码

    private DeviceInfo deviceInfo;
}
