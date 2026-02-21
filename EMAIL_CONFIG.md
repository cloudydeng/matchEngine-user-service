# 邮件服务配置说明

用户服务已集成邮件发送功能，实际发送验证码邮件。

## 当前配置

在 `application.yml` 中需要配置以下邮件服务参数：

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    protocol: smtps
    default-encoding: UTF-8
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 10000
          timeout: 10000
          writetimeout: 10000
      from: Match Engine <noreply@matchengine.com>
```

## 配置步骤

### 1. Gmail 配置（推荐用于测试）

1. 登录 Gmail 账户
2. 进入「设置」→「账户」→「安全性」
3. 启用「两步验证」
4. 点击「应用密码」→「生成应用密码」
   - 应用名称：Match Engine
   - 选择任意设备
5. 复制生成的应用专用密码（格式：`xxxx xxxx xxxx xxxx xxxx xxxx xxxx xxxx`）
6. 使用应用专用密码替换 `application.yml` 中的 `password` 字段

### 2. 企业邮箱配置

如果使用企业邮箱（如腾讯企业邮箱、阿里企业邮箱等），需要配置对应的 SMTP 服务器信息：

| 服务商 | SMTP 主机 | 端口 | 协议 |
|--------|-----------|------|------|
| Gmail | smtp.gmail.com | 587 | smtps |
| QQ 邮箱 | smtp.qq.com | 465 或 587 | smtp 或 smtps |
| 163 网易 | smtp.163.com | 465 或 994 | smtp 或 smtps |
| 腾讯企业 | smtp.exmail.qq.com | 465 | smtps |
| 阿里企业 | smtp.mxhichina.com | 465 | smtps |

### 3. 使用第三方邮件服务

也可以使用第三方邮件服务提供商（如 SendGrid、Mailgun、阿里云邮件推送等），需要：

1. 注册第三方邮件服务账号
2. 获取 API Key 或 SMTP 凭证
3. 更新 `application.yml` 配置
4. 修改 `EmailService.java` 使用对应的 SDK

## 测试配置

配置完成后，可以测试发送验证码：

```bash
# 启动用户服务
cd /Users/cloudy/IdeaProjects/matchEngine-user-service
mvn spring-boot:run

# 前端测试发送验证码
# 在浏览器打开 http://localhost:3000/forgot-password
# 输入邮箱地址，点击"发送验证码"
# 检查邮箱是否收到验证码
```

## 生产环境注意事项

生产环境请务必：

1. 使用企业邮箱或第三方邮件服务
2. 不要将邮箱密码硬编码在配置文件中
3. 使用环境变量或配置中心管理敏感信息
4. 配置邮件发送失败的重试机制
5. 设置合理的超时时间和限流策略

## 邮件模板说明

`EmailService.java` 中包含三种邮件模板：

1. **验证码邮件** (`sendVerificationCode`)
   - 蓝紫色渐变背景
   - 大号 6 位验证码显示
   - 5 分钟有效时间提示

2. **欢迎邮件** (`sendWelcomeEmail`)
   - 欢迎标题
   - 用户名显示
   - 立即登录按钮

3. **密码重置邮件** (`sendPasswordResetEmail`)
   - 蓝色警告框
   - 重要安全提示
   - 建议修改密码

## 故障排查

如果邮件无法发送：

1. 检查网络连接和防火墙设置
2. 确认 SMTP 服务器地址和端口正确
3. 验证邮箱账号密码（或应用密码）是否正确
4. 检查是否启用了两步验证（如 Gmail）
5. 查看应用日志中的具体错误信息
