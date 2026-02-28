package com.matching.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务 - 使用 SimpleMailMessage 避免类型冲突
 */
@Slf4j
@Service
@org.springframework.context.annotation.Profile("!test")
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    @Value("${app.name}")
    private String appName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送验证码邮件
     */
    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject("【" + appName + "】验证码");
            message.setText(buildEmailContent(code));
            message.setSentDate(new java.util.Date());

            mailSender.send(message);
            log.info("Verification code email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("发送验证码失败，请稍后重试");
        }
    }

    /**
     * 发送欢迎邮件
     */
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject("【" + appName + "】欢迎注册");
            message.setText(buildWelcomeContent(username));
            message.setSentDate(new java.util.Date());

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    /**
     * 发送密码重置邮件
     */
    public void sendPasswordResetEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject("【" + appName + "】密码已重置");
            message.setText(buildPasswordResetContent(username));
            message.setSentDate(new java.util.Date());

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    /**
     * 构建验证码邮件内容（简化文本格式）
     */
    private String buildEmailContent(String code) {
        StringBuilder content = new StringBuilder();
        content.append("【").append(appName).append("】\n\n");
        content.append("您的验证码是：").append(code).append("\n\n");
        content.append("验证码有效时间为：5 分钟\n\n");
        content.append("如果这不是您本人的操作，请忽略此邮件。\n\n");
        content.append("如有问题，请联系客服。");
        content.append("\n© 2026 ").append(appName).append(" | Match Engine");
        return content.toString();
    }

    /**
     * 构建欢迎邮件内容（简化文本格式）
     */
    private String buildWelcomeContent(String username) {
        StringBuilder content = new StringBuilder();
        content.append("【").append(appName).append("】\n\n");
        content.append("你好，").append(username).append("！\n\n");
        content.append("感谢您注册成为 Match Engine 的用户。\n\n");
        content.append("您的账号已经创建成功，现在可以开始使用我们的服务。\n\n");
        content.append("请访问 http://localhost:3000/login 立即登录。");
        content.append("\n\n© 2026 ").append(appName).append(" | Match Engine");
        return content.toString();
    }

    /**
     * 构建密码重置邮件内容（简化文本格式）
     */
    private String buildPasswordResetContent(String username) {
        StringBuilder content = new StringBuilder();
        content.append("【").append(appName).append("】\n\n");
        content.append("密码已重置\n\n");
        content.append("你好，").append(username).append("！\n\n");
        content.append("您的密码已成功重置。\n\n");
        content.append("【重要提示】\n");
        content.append("如果您没有进行此操作，请立即登录您的账号并修改密码。\n\n");
        content.append("如有问题，请联系客服。");
        content.append("\n\n© 2026 ").append(appName).append(" | Match Engine");
        return content.toString();
    }
}
