package com.matching.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 通用响应格式
 */
@Data
@Builder
@AllArgsConstructor
public class CommonResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .code(0)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> CommonResponse<T> error(String message) {
        return CommonResponse.<T>builder()
                .code(1)
                .message(message)
                .data(null)
                .build();
    }
}
