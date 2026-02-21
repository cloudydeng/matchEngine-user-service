package com.matching.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * 邮件服务
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
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject("【" + appName + "】验证码");

            String content = buildEmailContent(code);
            message.setText(content, true);
            message.setSentDate(new java.util.Date());

            mailSender.send(message);
            log.info("Verification code email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("发送验证码失败，请稍后重试");
        }
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f5f7fa; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: hidden; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; }" +
                ".header h1 { color: white; margin: 0; font-size: 24px; }" +
                ".code { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; font-size: 48px; font-weight: bold; padding: 20px 40px; border-radius: 10px; margin: 30px 0; letter-spacing: 8px; }" +
                ".info { padding: 0 40px; text-align: center; color: #666; }" +
                ".info p { margin: 10px 0; line-height: 1.6; }" +
                ".footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #999; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>验证码</h1>" +
                "</div>" +
                "<div class='code'>" + code + "</div>" +
                "<div class='info'>" +
                "<p>您的验证码是：</p>" +
                "<p style='font-size: 18px; color: #333; font-weight: bold;'>" + code + "</p>" +
                "<p>验证码有效时间为 <strong>5 分钟</strong></p>" +
                "<p>如果这不是您本人的操作，请忽略此邮件。</p>" +
                "</div>" +
                "<div class='footer'>" +
                "如有问题，请联系客服<br>" +
                "© 2026 " + appName + " | Match Engine" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 发送欢迎邮件
     */
    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject("【" + appName + "】欢迎注册");

            String content = buildWelcomeContent(username);
            message.setText(content, true);
            message.setSentDate(new java.util.Date());

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    private String buildWelcomeContent(String username) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f5f7fa; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: hidden; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; }" +
                ".header h1 { color: white; margin: 0; font-size: 24px; }" +
                ".content { padding: 30px; }" +
                ".content h2 { color: #333; margin-top: 0; }" +
                ".content p { color: #666; line-height: 1.6; }" +
                ".button { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin-top: 20px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>欢迎加入 " + appName + "</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>你好，" + username + "！</h2>" +
                "<p>感谢您注册成为 " + appName + " 的用户。</p>" +
                "<p>您的账号已经创建成功，现在可以开始使用我们的服务。</p>" +
                "<a href='http://localhost:3000/login' class='button'>立即登录</a>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 发送密码重置邮件
     */
    public void sendPasswordResetEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(from);
            message.setTo(toEmail);
            message.setSubject("【" + appName + "】密码已重置");

            String content = buildPasswordResetContent(username);
            message.setText(content, true);
            message.setSentDate(new java.util.Date());

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    private String buildPasswordResetContent(String username) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f5f7fa; margin: 0; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); overflow: hidden; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; }" +
                ".header h1 { color: white; margin: 0; font-size: 24px; }" +
                ".content { padding: 30px; }" +
                ".content h2 { color: #333; margin-top: 0; }" +
                ".content p { color: #666; line-height: 1.6; }" +
                ".alert { background-color: #fff3cd; border-left: 4px solid #667eea; padding: 15px; margin: 20px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>密码已重置</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>你好，" + username + "！</h2>" +
                "<p>您的密码已成功重置。</p>" +
                "<div class='alert'>" +
                "<p><strong>重要提示：</strong></p>" +
                "<p>如果您没有进行此操作，请立即登录您的账号并修改密码。</p>" +
                "</div>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
