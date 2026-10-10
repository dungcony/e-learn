package com.restaurant.modules.user.dto.request;

import jakarta.validation.constraints.NotBlank;

/** @param token token thô lấy từ liên kết xác thực trong email */
public record EmailVerifyRequest(@NotBlank(message = "Token không được để trống") String token) {
}
