package com.elearning.user.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Cấu hình xác thực và tài khoản.
 *
 * @param frontendUrl            gốc URL frontend, ghép thành link đặt lại mật khẩu trong email
 * @param resetTokenTtl          thời gian sống của token đặt lại mật khẩu (SRS: 60 phút)
 * @param bootstrapAdminEmail    email QTV đầu tiên; bỏ trống thì không tự tạo
 * @param bootstrapAdminPassword mật khẩu QTV đầu tiên; bỏ trống thì không tự tạo
 */
@Validated
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        @NotBlank String frontendUrl,
        @NotNull Duration resetTokenTtl,
        String bootstrapAdminEmail,
        String bootstrapAdminPassword
) {
}
