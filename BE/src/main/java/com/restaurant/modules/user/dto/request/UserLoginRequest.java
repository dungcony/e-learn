package com.restaurant.modules.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Yêu cầu đăng nhập (UC001). Không kiểm tra độ mạnh mật khẩu ở đây: chỉ so với mật khẩu đã lưu.
 */
public record UserLoginRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        String password) {
}
