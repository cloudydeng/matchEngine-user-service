package com.matching.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 注册请求
 */
@Data
@Builder
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度3-32")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字、下划线")
    private String username;

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度8-32")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Pattern(regexp = "^1[3-9]{6}$", message = "验证码格式不正确")
    private String emailVerificationCode;  // 邮箱验证码

    @Pattern(regexp = "^1[3-9]{6}$", message = "验证码格式不正确")
    private String phoneVerificationCode;  // 手机验证码

    private String referralCode;  // 推荐码

    private String phone;  // 手机号

    private DeviceInfo deviceInfo;
}
