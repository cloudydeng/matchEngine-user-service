package com.matching.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "user-service");
        response.put("version", "1.0.0");
        response.put("encryption", "BCrypt");
        return response;
    }

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User Service is running");
        response.put("endpoints", new String[]{
            "/health",
            "/register",
            "/login",
            "/refresh",
            "/info",
            "/verify/send"
        });
        return response;
    }
}
