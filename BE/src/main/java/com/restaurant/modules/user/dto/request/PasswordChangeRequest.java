package com.restaurant.modules.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Đổi mật khẩu khi đã đăng nhập (UC003).
 *
 * @param oldPassword        mật khẩu hiện tại
 * @param newPassword        ít nhất 8 ký tự gồm chữ và số, khác mật khẩu cũ
 * @param confirmNewPassword phải trùng {@code newPassword}
 */
public record PasswordChangeRequest(
        @NotBlank(message = "Mật khẩu cũ không được để trống")
        String oldPassword,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = PasswordRules.MIN_LENGTH, max = PasswordRules.MAX_LENGTH, message = PasswordRules.LENGTH_MESSAGE)
        @Pattern(regexp = PasswordRules.PATTERN, message = PasswordRules.PATTERN_MESSAGE)
        String newPassword,

        @NotBlank(message = "Xác nhận mật khẩu mới không được để trống")
        String confirmNewPassword) {
}
