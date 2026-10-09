package com.elearning.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Yêu cầu gửi link đặt lại mật khẩu (UC003).
 *
 * @param email email của tài khoản quên mật khẩu
 */
public record PasswordForgotRequest(
        @NotBlank(message = "Email không được để trống") @Email(message = "Email không đúng định dạng") String email
) {
}
