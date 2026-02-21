package com.matching.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送验证码响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeResponse {
    private String message;
    private boolean success;
}
