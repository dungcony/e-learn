package com.restaurant.modules.user.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Cấu hình xác thực, đọc từ {@code app.auth.*}.
 *
 * @param frontendUrl            gốc URL frontend, dùng dựng liên kết trong email
 * @param resetTokenTtl          hiệu lực liên kết đặt lại mật khẩu (SRS: 60 phút)
 * @param verifyTokenTtl         hiệu lực liên kết xác thực email (SRS: 24 giờ)
 * @param bootstrapAdminEmail    email Quản lý đầu tiên; trống thì không tạo
 * @param bootstrapAdminPassword mật khẩu Quản lý đầu tiên; trống thì không tạo
 */
@Validated
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        @NotBlank String frontendUrl,
        @NotNull Duration resetTokenTtl,
        @NotNull Duration verifyTokenTtl,
        String bootstrapAdminEmail,
        String bootstrapAdminPassword) {
}
