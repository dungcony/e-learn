package com.elearning.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Học viên đổi mật khẩu (UC002); phải nhập mật khẩu cũ để xác minh.
 *
 * @param oldPassword     mật khẩu hiện tại
 * @param newPassword     mật khẩu mới, tối thiểu 6 ký tự
 * @param confirmPassword nhập lại mật khẩu mới, phải trùng {@code newPassword}
 */
public record PasswordChangeRequest(
        @NotBlank(message = "Mật khẩu cũ không được để trống") String oldPassword,
        @NotBlank(message = "Mật khẩu mới không được để trống") @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự") String newPassword,
        @NotBlank(message = "Xác nhận mật khẩu không được để trống") String confirmPassword
) {
}
