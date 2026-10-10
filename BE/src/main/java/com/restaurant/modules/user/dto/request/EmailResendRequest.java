package com.restaurant.modules.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** @param email email của tài khoản cần gửi lại liên kết xác thực */
public record EmailResendRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email) {
}
