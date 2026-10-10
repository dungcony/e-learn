package com.restaurant.modules.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Yêu cầu đăng ký tài khoản khách hàng (UC002).
 *
 * @param fullName        họ tên, tối đa 255 ký tự
 * @param email           email đăng nhập; được chuẩn hóa chữ thường trước khi lưu
 * @param phone           số điện thoại 10 chữ số; tùy chọn
 * @param password        ít nhất 8 ký tự gồm chữ và số
 * @param confirmPassword phải trùng {@code password}
 */
public record UserRegisterRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 255, message = "Họ tên tối đa 255 ký tự")
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 255, message = "Email tối đa 255 ký tự")
        String email,

        @Pattern(regexp = "\\d{10}", message = "Số điện thoại phải gồm 10 chữ số")
        String phone,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = PasswordRules.MIN_LENGTH, max = PasswordRules.MAX_LENGTH, message = PasswordRules.LENGTH_MESSAGE)
        @Pattern(regexp = PasswordRules.PATTERN, message = PasswordRules.PATTERN_MESSAGE)
        String password,

        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        String confirmPassword) {
}
