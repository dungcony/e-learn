package com.elearning.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Yêu cầu đăng ký tài khoản học viên (UC004, Bảng 2-6).
 *
 * @param email           email, dùng làm tên đăng nhập
 * @param password        mật khẩu, tối thiểu 6 ký tự
 * @param confirmPassword nhập lại mật khẩu, phải trùng {@code password}
 */
public record UserRegisterRequest(
        @NotBlank(message = "Email không được để trống") @Email(message = "Email không đúng định dạng") String email,
        @NotBlank(message = "Mật khẩu không được để trống") @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự") String password,
        @NotBlank(message = "Xác nhận mật khẩu không được để trống") String confirmPassword
) {
}
