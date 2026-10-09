package com.elearning.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Đặt mật khẩu mới bằng token trong link email (UC003).
 *
 * @param token           token lấy từ link, còn hạn 60 phút và dùng được một lần
 * @param newPassword     mật khẩu mới, tối thiểu 6 ký tự
 * @param confirmPassword nhập lại mật khẩu mới, phải trùng {@code newPassword}
 */
public record PasswordResetRequest(
        @NotBlank(message = "Token không được để trống") String token,
        @NotBlank(message = "Mật khẩu mới không được để trống") @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự") String newPassword,
        @NotBlank(message = "Xác nhận mật khẩu không được để trống") String confirmPassword
) {
}
