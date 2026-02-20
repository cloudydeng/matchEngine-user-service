package com.matching.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 设备信息
 */
@Data
@Builder
@AllArgsConstructor
public class DeviceInfo {
    private String deviceId;       // 设备唯一标识
    private String deviceType;     // WEB, IOS, ANDROID
    private String deviceName;     // Chrome, Safari, etc.
    private String ipAddress;      // IP 地址
    private String userAgent;      // User Agent
}
