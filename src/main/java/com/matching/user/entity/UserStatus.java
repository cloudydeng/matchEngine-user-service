package com.matching.user.entity;

/**
 * 用户状态枚举
 */
public enum UserStatus {
    ACTIVE,      // 激活
    DISABLED,    // 禁用
    LOCKED,      // 锁定
    DELETED,     // 已删除
    PENDING_VERIFICATION  // 待验证
}
