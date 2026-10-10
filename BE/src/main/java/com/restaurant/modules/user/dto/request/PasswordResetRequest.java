package com.restaurant.modules.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Đặt mật khẩu mới bằng token trong liên kết email (UC004).
 *
 * @param token              token thô lấy từ liên kết
 * @param newPassword        ít nhất 8 ký tự gồm chữ và số
 * @param confirmNewPassword phải trùng {@code newPassword}
 */
public record PasswordResetRequest(
        @NotBlank(message = "Token không được để trống")
        String token,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = PasswordRules.MIN_LENGTH, max = PasswordRules.MAX_LENGTH, message = PasswordRules.LENGTH_MESSAGE)
        @Pattern(regexp = PasswordRules.PATTERN, message = PasswordRules.PATTERN_MESSAGE)
        String newPassword,

        @NotBlank(message = "Xác nhận mật khẩu mới không được để trống")
        String confirmNewPassword) {
}
