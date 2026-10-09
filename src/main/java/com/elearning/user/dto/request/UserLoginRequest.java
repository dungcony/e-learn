package com.elearning.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Yêu cầu đăng nhập (UC001).
 *
 * @param email    email tài khoản
 * @param password mật khẩu, tối thiểu 6 ký tự (Bảng 2-2)
 */
public record UserLoginRequest(
        @NotBlank(message = "Email không được để trống") @Email(message = "Email không đúng định dạng") String email,
        @NotBlank(message = "Mật khẩu không được để trống") @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự") String password
) {
}
